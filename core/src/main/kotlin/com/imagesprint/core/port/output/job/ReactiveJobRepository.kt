package com.imagesprint.core.port.output.job

import com.imagesprint.core.domain.job.Job
import com.imagesprint.core.port.input.job.JobStatus
import java.time.LocalDateTime

interface ReactiveJobRepository {
    suspend fun save(job: Job): Job

    suspend fun deleteAll()

    suspend fun getJob(jobId: Long): Job?

    suspend fun updateStatus(
        jobId: Long,
        jobStatus: JobStatus,
        createdAt: LocalDateTime,
    )

    suspend fun incrementDoneCount(jobId: Long)

    suspend fun updateSummary(
        jobId: Long,
        status: JobStatus,
        doneCount: Int,
        convertedSize: Long,
        finishedAt: LocalDateTime,
        expiredAt: LocalDateTime?,
        zipUrl: String,
    )
}
