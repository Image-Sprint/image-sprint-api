package com.imagesprint.core.port.output.job

import com.imagesprint.core.domain.job.Job

interface JobRepository {
    fun saveJob(job: Job): Job

    fun getMyJobs(userId: Long): List<Job>
}
