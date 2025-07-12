package com.imagesprint.core.port.input.job

data class JobProgressResult(
    val jobId: Long,
    val doneCount: Int,
    val imageCount: Int,
)
