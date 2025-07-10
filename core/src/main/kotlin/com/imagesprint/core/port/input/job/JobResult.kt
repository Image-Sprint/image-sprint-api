package com.imagesprint.core.port.input.job

import java.time.LocalDateTime

data class JobResult(
    val jobId: Long,
    val status: String,
    val imageCount: Int,
    val originalSize: Long,
    val createdAt: LocalDateTime,
    val zirUrl: String? = null,
)
