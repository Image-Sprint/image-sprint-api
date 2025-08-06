package com.imagesprint.core.port.output.job

import com.imagesprint.core.domain.job.Job
import com.imagesprint.core.port.input.job.JobPage

interface JobRepository {
    fun saveJob(job: Job): Job

    fun getMyJobs(userId: Long): List<Job>

    fun getMyJobsByCursor(
        userId: Long,
        cursor: Long?,
        pageSize: Int,
    ): JobPage
}
