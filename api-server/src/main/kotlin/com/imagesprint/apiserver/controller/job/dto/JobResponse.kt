package com.imagesprint.apiserver.controller.job.dto

import com.imagesprint.core.domain.job.Job
import com.imagesprint.core.port.input.job.JobResult
import java.time.LocalDateTime

data class JobResponse(
    val jobId: Long,
    val status: String,
    val imageCount: Int,
    val originalSize: Long,
    val createdAt: LocalDateTime,
    val expiredAt: LocalDateTime? = null,
    val zipUrl: String? = null,
) {
    companion object {
        fun from(result: JobResult): JobResponse =
            JobResponse(
                result.jobId,
                result.status,
                result.imageCount,
                result.originalSize,
                result.createdAt,
                null,
                result.zirUrl,
            )

        fun from(result: Job): JobResponse =
            JobResponse(
                result.jobId!!,
                result.status.name,
                result.imageCount,
                result.originalSize,
                result.createdAt,
                result.expiredAt,
                result.zipUrl,
            )
    }
}
