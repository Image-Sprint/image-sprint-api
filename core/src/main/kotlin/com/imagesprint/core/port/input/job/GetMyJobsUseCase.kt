package com.imagesprint.core.port.input.job

interface GetMyJobsUseCase {
    fun getMyJobs(query: GetJobPageQuery): JobPage
}
