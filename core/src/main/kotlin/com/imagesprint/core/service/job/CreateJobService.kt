package com.imagesprint.core.service.job

import com.imagesprint.core.domain.job.ConversionOption
import com.imagesprint.core.domain.job.ImageFile
import com.imagesprint.core.domain.job.Job
import com.imagesprint.core.exception.CustomException
import com.imagesprint.core.exception.ErrorCode
import com.imagesprint.core.port.input.job.*
import com.imagesprint.core.port.output.job.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class CreateJobService(
    private val jobRepository: JobRepository,
    private val imageRepository: ImageRepository,
    private val conversionOptionRepository: ConversionOptionRepository,
    private val fileStoragePort: FileStoragePort,
    private val jobQueuePort: JobQueuePort,
) : CreateJobUseCase {
    private val logger = LoggerFactory.getLogger(CreateJobService::class.java)

    /**
     * 이미지 변환 Job을 생성하는 유스케이스 실행 메서드.
     *
     * 이 메서드는 다음 단계를 수행한다:
     * 1. 입력 커맨드 유효성 검증
     * 2. 이미지 메타 정보를 저장하고 DB에 등록
     * 3. Job 엔티티를 생성 및 저장
     * 4. 변환 옵션을 저장
     * 5. 이미지와 Job을 매핑
     * 6. 외부 파일 저장 시스템에 원본 이미지 저장 요청
     * 7. Redis 큐에 변환 작업을 등록
     * 8. 최종적으로 Job 결과 정보를 반환
     *
     * 예외 상황:
     * - 파일 저장 실패 시 `FILE_STORAGE_FAILED` 예외 발생
     * - 큐 등록 실패 시 `QUEUE_ENQUEUE_FAILED` 예외 발생
     *
     * @param command Job 생성을 위한 입력 커맨드
     * @return 생성된 Job의 결과 정보
     * @throws CustomException 저장/큐 등록 실패 등 비즈니스 예외
     */
    @Transactional
    override fun execute(command: CreateJobCommand): JobResult {
        command.validate()

        // 1. 이미지 메타 저장
        val imageFiles =
            command.fileMetas.map { meta ->
                ImageFile(
                    jobId = null,
                    fileName = meta.originalFilename,
                    format = meta.contentType,
                    size = meta.size,
                    convertedSize = null,
                    convertStatus = ConvertStatus.WAITING,
                    errorMessage = null,
                    createdAt = LocalDateTime.now(),
                )
            }

        val savedImages = imageRepository.saveImages(imageFiles)
        val savedImageMetas =
            savedImages.map {
                SavedImageMeta(imageFileId = it.imageFileId!!, size = it.size)
            }

        // 2. Job 생성
        val job =
            Job(
                userId = command.userId,
                jobName = generateJobName(),
                status = JobStatus.PENDING,
                originalSize = savedImages.sumOf { it.size },
                convertedSize = null,
                imageCount = savedImages.size,
                doneCount = null,
                createdAt = LocalDateTime.now(),
                finishedAt = null,
                expiredAt = null,
            )
        val savedJob = jobRepository.saveJob(job)
        val jobId = savedJob.jobId ?: throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR)

        // 3. 변환 옵션 저장
        val option =
            ConversionOption(
                jobId = jobId,
                resizeWidth = command.resizeWidth,
                resizeHeight = command.resizeHeight,
                keepRatio = command.keepRatio,
                toFormat = command.toFormat,
                quality = command.quality,
                watermarkText = command.watermarkText,
                watermarkPosition = command.watermarkPosition,
                watermarkOpacity = command.watermarkOpacity,
            )
        conversionOptionRepository.saveOption(option)

        // 4. image_file ↔ job 매핑
        imageRepository.updateJobIdAndStatus(
            jobId = jobId,
            imageIds = savedImages.map { it.imageFileId!! },
        )

        // 5. 외부 시스템 처리 (파일 저장 + 큐 등록)
        try {
            fileStoragePort.saveOriginalFiles(
                userId = command.userId,
                files = command.fileMetas,
                savedImages = savedImageMetas,
            )
        } catch (e: Exception) {
            logger.error("파일 저장 실패. 롤백 수행됨. JobId: {}", jobId, e)
            throw CustomException(ErrorCode.FILE_STORAGE_FAILED)
        }

        try {
            jobQueuePort.enqueueJob(
                jobId = jobId,
                userId = command.userId,
                imageIds = savedImages.map { it.imageFileId!! },
            )
        } catch (e: Exception) {
            logger.error("큐 등록 실패. 롤백 수행됨. JobId: {}", jobId, e)
            throw CustomException(ErrorCode.QUEUE_ENQUEUE_FAILED)
        }

        // 6. 응답
        return JobResult(
            jobId = jobId,
            status = savedJob.status.name,
            imageCount = savedImages.size,
            originalSize = savedImages.sumOf { it.size },
        )
    }

    private fun generateJobName(): String = "Job-${UUID.randomUUID()}"
}
