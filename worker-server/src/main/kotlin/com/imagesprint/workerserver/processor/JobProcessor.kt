package com.imagesprint.workerserver.processor

import com.imagesprint.core.port.input.job.JobStatus
import com.imagesprint.workerserver.client.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * 주어진 Job ID에 대해 이미지 변환 작업을 수행하고,
 * 결과를 S3에 업로드한 뒤 DB 상태 및 웹훅, 알림 처리를 수행합니다.
 *
 * 처리 순서:
 * 1. Job 정보 조회 및 유효성 검증
 * 2. Job 상태를 PROCESSING으로 마킹
 * 3. 이미지들을 병렬로 처리 (리사이즈, 포맷변환 등)
 * 4. 결과 집계 (성공/실패 개수 및 총 용량)
 * 5. 변환 결과를 ZIP으로 묶어 S3에 업로드
 * 6. Job 요약 정보 저장 (상태, 용량, 완료 개수 등)
 * 7. 알림 및 웹훅 전송
 * 8. 임시 파일 정리
 *
 * 실패 이미지가 있어도 나머지를 처리하며, ZIP 업로드 실패 시에도 가능한 정보를 저장하고 후속 처리를 진행합니다.
 *
 * @param jobId 처리할 Job의 ID
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
) {
    private val logger = LoggerFactory.getLogger(JobProcessor::class.java)

    suspend fun process(jobId: Long) =
        coroutineScope {
            logger.info("[Worker] JobProcessor invoked with jobId = $jobId")

            // 1. Job 조회
            val job = jobReader.getJob(jobId)
            if (job == null) {
                logger.warn("[Worker] Job not found: $jobId")
                return@coroutineScope
            }

            // 2. 처리 시작 알림
            notifier.notifyStarted(job)

            // 3. 옵션 및 이미지 조회
            val option = jobReader.getOption(jobId)
            val images = jobReader.getImages(jobId)

            // 4. 상태를 PROCESSING으로 변경
            jobWriter.markProcessing(jobId)

            // 5. 이미지 병렬 처리
            val results =
                images
                    .map { image ->
                        async(Dispatchers.IO) {
                            val result = imageProcessor.processImage(jobId, image, option)

                            if (result.isSuccess) {
                                val convertedSize = result.getOrNull() ?: 0L
                                // 처리 진척도 및 이미지 크기 업데이트
                                jobWriter.incrementProgress(jobId, convertedSize)
                                jobWriter.updateImageSize(image.imageFileId!!, convertedSize)
                            }

                            result
                        }
                    }.awaitAll()

            // 6. 성공/실패 개수 및 전체 크기 계산
            val successCount = results.count { it.isSuccess }
            val failedCount = results.size - successCount
            val totalConvertedSize = results.sumOf { it.getOrNull() ?: 0L }

            // 7. ZIP 파일 생성 및 업로드 (예외 발생 가능)
            var zipUrl: String? = null
            try {
                val zipFile = imageProcessor.zipConvertedImages(jobId)
                zipUrl = s3ClientAdapter.generatePresignedUrl(job.userId, jobId)
                uploader.upload(zipUrl, zipFile)
            } catch (e: Exception) {
                logger.error("[Worker] ZIP upload failed", e)
            }

            // 8. 작업 요약 저장 (성공 여부 포함)
            jobWriter.summarize(
                jobId = jobId,
                status = if (failedCount == 0) JobStatus.DONE else JobStatus.FAILED,
                doneCount = successCount,
                convertedSize = totalConvertedSize,
                zipUrl = zipUrl ?: "",
            )

            // 9. 완료 알림 및 웹훅 전송
            notifier.notifyFinished(job, failedCount == 0)
            webhookDispatcher.dispatch(job.userId, jobId, if (failedCount == 0) JobStatus.DONE else JobStatus.FAILED)

            // 10. 임시 파일 정리
            imageProcessor.cleanTempFiles(jobId)

            logger.info("[Worker] Job processed: $jobId, success=$successCount, failed=$failedCount")
        }
}
