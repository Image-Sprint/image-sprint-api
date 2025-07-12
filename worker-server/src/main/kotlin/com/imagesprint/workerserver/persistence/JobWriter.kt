package com.imagesprint.workerserver.persistence

import com.imagesprint.core.port.input.job.JobStatus
import com.imagesprint.core.port.output.job.ReactiveImageRepository
import com.imagesprint.core.port.output.job.ReactiveJobRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class JobWriter(
    private val jobRepository: ReactiveJobRepository,
    private val imageRepository: ReactiveImageRepository,
) {
    suspend fun markProcessing(jobId: Long) {
        jobRepository.updateStatus(jobId, JobStatus.PROCESSING, LocalDateTime.now())
    }

    suspend fun updateImageSize(
        imageFileId: Long,
        convertedSize: Long,
    ) {
        imageRepository.updateConvertedSize(imageFileId, convertedSize)
    }

    suspend fun summarize(
        jobId: Long,
        status: JobStatus,
        doneCount: Int,
        convertedSize: Long,
        zipUrl: String,
        expiredAt: LocalDateTime?,
    ) {
        jobRepository.updateSummary(
            jobId = jobId,
            status = status,
            doneCount = doneCount,
            convertedSize = convertedSize,
            finishedAt = LocalDateTime.now(),
            expiredAt = expiredAt,
            zipUrl = zipUrl,
        )
    }
}
