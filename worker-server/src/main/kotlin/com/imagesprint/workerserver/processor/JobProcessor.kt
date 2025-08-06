package com.imagesprint.workerserver.processor

import com.imagesprint.core.port.input.job.JobStatus
import com.imagesprint.core.port.output.job.ReactiveJobProgressRedisPort
import com.imagesprint.workerserver.client.*
import com.imagesprint.workerserver.persistence.JobNotifier
import com.imagesprint.workerserver.persistence.JobReader
import com.imagesprint.workerserver.persistence.JobWriter
import com.imagesprint.workerserver.persistence.WebhookDispatcher
import com.imagesprint.workerserver.publisher.JobProgressRedisPublisher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 주어진 Job ID에 대해 이미지 변환 작업을 수행하는 워커 프로세서.
 *
 * 처리 단계:
 * 1. 작업 조회 및 유효성 검증
 * 2. 상태를 PROCESSING으로 변경하고 시작 알림 전송
 * 3. 옵션 및 이미지 목록 조회, Redis에 진행률 초기화
 * 4. 이미지를 병렬로 변환하며:
 *    - Redis의 진행률 정보(doneCount) 갱신
 *    - 현재 진행률을 Redis Pub/Sub으로 발행 (SSE를 통한 실시간 전송용)
 *    - DB에 개별 이미지의 결과 크기 저장
 * 5. 모든 이미지 처리 후 ZIP 파일 생성 및 Presigned URL 업로드
 * 6. 변환 결과 요약 정보를 DB에 저장 (진행 상태, 총 크기, 성공/실패 개수, ZIP URL 등)
 * 7. 작업 완료 알림 전송 및 등록된 웹훅 호출
 * 8. 임시 파일 삭제 및 Redis의 진행률 정보 제거
 *
 * 일부 이미지 변환에 실패하더라도 가능한 범위 내에서 처리를 계속 진행하며,
 * ZIP 업로드가 실패해도 DB 상태 저장, 알림, 웹훅 호출은 정상적으로 수행됩니다.
 *
 * @param jobId 처리할 작업의 고유 식별자
 */
@Profile("worker")
@Component
class JobProcessor(
    private val jobReader: JobReader,
    private val jobWriter: JobWriter,
    private val imageProcessor: ImageProcessor,
    private val s3ClientAdapter: S3ClientAdapter,
    private val uploader: HttpZipUploader,
    private val notifier: JobNotifier,
    private val webhookDispatcher: WebhookDispatcher,
    private val jobProgressRedisPort: ReactiveJobProgressRedisPort,
    private val publisher: JobProgressRedisPublisher,
) {
    private val logger = LoggerFactory.getLogger(JobProcessor::class.java)

    suspend fun process(jobId: Long) =
        coroutineScope {
            logger.info("[Worker] JobProcessor invoked with jobId = $jobId")

            val job = jobReader.getJob(jobId)
            if (job == null) {
                logger.warn("[Worker] Job not found: $jobId")
                return@coroutineScope
            }

            var results: List<Result<Long>> = emptyList()
            var downloadUrl: String? = null
            var finalStatus = JobStatus.FAILED
            var doneCount = 0
            var imageCount = 0
            var totalConvertedSize = 0L

            try {
                // 1. 시작 알림 전송
                notifier.notifyStarted(job)

                // 2. 옵션 및 이미지 목록 조회
                val option = jobReader.getOption(jobId)
                val images = jobReader.getImages(jobId)

                // 3. 상태 PROCESSING으로 변경
                jobWriter.markProcessing(jobId)

                // 4. Redis 진행률 초기화
                jobProgressRedisPort.initProgress(jobId, images.size)

                // 5. 이미지 변환 병렬 처리 (성공 시 doneCount 증가 및 Pub/Sub)
                results =
                    images
                        .map { image ->
                            async(Dispatchers.Default) {
                                val result = imageProcessor.processImage(jobId, image, option)

                                if (result.isSuccess) {
                                    val convertedSize = result.getOrNull() ?: 0L

                                    // Redis에 doneCount 증가
                                    jobProgressRedisPort.incrementDone(jobId)

                                    // 현재 진행률 조회 후 Pub/Sub 발행
                                    val (done, total) = jobProgressRedisPort.getProgress(jobId)
                                    publisher.publish(jobId, done, total).awaitSingle()

                                    // DB에 개별 이미지 결과 기록
                                    jobWriter.updateImageSize(image.imageFileId!!, convertedSize)
                                }

                                result
                            }
                        }.awaitAll()

                // 6. 최종 진행률 및 변환 용량 계산
                doneCount = jobProgressRedisPort.getProgress(jobId).first
                imageCount = images.size
                totalConvertedSize = results.sumOf { it.getOrNull() ?: 0L }

                // 7. ZIP 생성 및 Presigned URL 업로드
                try {
                    val zipFile = imageProcessor.zipConvertedImages(jobId)
                    val uploadUrl = s3ClientAdapter.generatePresignedUploadUrl(job.userId, jobId)
                    uploader.upload(uploadUrl, zipFile)
                } catch (e: Exception) {
                    logger.error("[Worker] ZIP upload failed", e)
                }

                // 8. 다운로드용 Presigned URL 생성
                downloadUrl = s3ClientAdapter.generatePresignedDownloadUrl(job.userId, jobId)

                // 9. 최종 상태 결정
                finalStatus = if (doneCount == imageCount) JobStatus.DONE else JobStatus.FAILED
            } catch (e: Exception) {
                // 중간 단계 실패 로깅
                logger.error("[Worker] Job processing error for jobId = $jobId", e)
            } finally {
                // 10. 요약 정보 DB 반영 (실패하더라도 시도)
                try {
                    jobWriter.summarize(
                        jobId = jobId,
                        status = finalStatus,
                        doneCount = doneCount,
                        convertedSize = totalConvertedSize,
                        zipUrl = downloadUrl ?: "",
                        expiredAt = if (finalStatus == JobStatus.DONE) LocalDateTime.now().plusDays(1) else null,
                    )
                } catch (e: Exception) {
                    logger.error("[Worker] summarize() 실패", e)
                }

                // 11. 알림 및 웹훅 전송
                try {
                    val failedCount = results.count { it.isFailure }
                    notifier.notifyFinished(job, failedCount == 0)
                    webhookDispatcher.dispatch(
                        userId = job.userId,
                        jobId = jobId,
                        status = finalStatus,
                    )
                } catch (e: Exception) {
                    logger.error("[Worker] 알림/웹훅 처리 실패", e)
                }

                // 12. 임시 파일 정리
                try {
                    imageProcessor.cleanTempFiles(jobId)
                } catch (e: Exception) {
                    logger.warn("[Worker] 임시 파일 정리 실패: ${e.message}")
                }

                // 13. Redis 진행률 제거 (SSE 에러 방지를 위해 중요)
                try {
                    jobProgressRedisPort.removeProgress(jobId)
                } catch (e: Exception) {
                    logger.warn("[Worker] Redis 진행률 제거 실패: ${e.message}")
                }

                // 14. 로그 출력
                logger.info(
                    "[Worker] Job 완료: $jobId, status=$finalStatus, success=${
                        results.count {
                            it.isSuccess
                        }
                    }, failed=${results.count { it.isFailure }}",
                )
            }
        }
}
