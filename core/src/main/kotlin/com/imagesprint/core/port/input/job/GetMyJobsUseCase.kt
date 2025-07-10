package com.imagesprint.core.port.input.job

interface GetMyJobsUseCase {
    fun getMyJobs(userId: Long): List<JobResult>
}
