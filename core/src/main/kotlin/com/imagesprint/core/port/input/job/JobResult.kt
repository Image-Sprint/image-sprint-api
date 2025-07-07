package com.imagesprint.core.port.input.job

data class JobResult(
    val jobId: Long,
    val status: String,
    val imageCount: Int,
    val originalSize: Long,
)
