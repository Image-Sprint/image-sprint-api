package com.imagesprint.core.port.input.job

import com.imagesprint.core.domain.job.Job

data class JobPage(
    val jobs: List<Job>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)
