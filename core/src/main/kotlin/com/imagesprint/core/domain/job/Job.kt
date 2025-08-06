package com.imagesprint.core.domain.job

import com.imagesprint.core.port.input.job.JobStatus
import java.time.LocalDateTime

data class Job(
    val jobId: Long? = null,
    val userId: Long,
    val jobName: String,
    val status: JobStatus,
    val zipUrl: String? = null,
    val originalSize: Long,
    val convertedSize: Long? = null,
    val imageCount: Int,
    val doneCount: Int? = null,
    val createdAt: LocalDateTime,
    val finishedAt: LocalDateTime? = null,
    val expiredAt: LocalDateTime? = null,
)
