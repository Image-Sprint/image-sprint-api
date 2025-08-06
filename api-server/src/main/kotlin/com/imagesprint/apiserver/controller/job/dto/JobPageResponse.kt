package com.imagesprint.apiserver.controller.job.dto

import com.imagesprint.core.port.input.job.JobPage

data class JobPageResponse(
    val jobs: List<JobResponse>,
    val nextCursor: Long?,
    val hasNext: Boolean,
) {
    companion object {
        fun from(page: JobPage): JobPageResponse =
            JobPageResponse(
                jobs = page.jobs.map { JobResponse.from(it) },
                nextCursor = page.nextCursor,
                hasNext = page.hasNext,
            )
    }
}
