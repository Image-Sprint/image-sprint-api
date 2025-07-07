package com.imagesprint.core.support.factory

import com.imagesprint.core.domain.job.Job
import com.imagesprint.core.port.input.job.JobStatus
import java.time.LocalDateTime

object JobTestFactory {
    fun create(
        jobId: Long? = 1L,
        userId: Long = 1L,
        jobName: String = "Job-UUID",
        status: JobStatus = JobStatus.PENDING,
        originalSize: Long = 123456,
        convertedSize: Long? = null,
        imageCount: Int = 2,
        doneCount: Int? = null,
        createdAt: LocalDateTime = LocalDateTime.now(),
        finishedAt: LocalDateTime? = null,
        expiredAt: LocalDateTime? = null,
    ): Job =
        Job(
            jobId = jobId,
            userId = userId,
            jobName = jobName,
            status = status,
            originalSize = originalSize,
            convertedSize = convertedSize,
            imageCount = imageCount,
            doneCount = doneCount,
            createdAt = createdAt,
            finishedAt = finishedAt,
            expiredAt = expiredAt,
        )
}
