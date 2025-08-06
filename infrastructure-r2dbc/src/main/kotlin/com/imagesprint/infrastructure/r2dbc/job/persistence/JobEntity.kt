package com.imagesprint.infrastructure.r2dbc.job.persistence

import com.imagesprint.core.domain.job.Job
import com.imagesprint.core.port.input.job.JobStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("job")
data class JobEntity(
    @Id
    val jobId: Long? = null,
    val userId: Long,
    val jobName: String,
    val status: String,
    val zipUrl: String? = null,
    val originalSize: Long,
    val convertedSize: Long? = null,
    val imageCount: Int,
    val doneCount: Int? = null,
    val createdAt: LocalDateTime,
    val finishedAt: LocalDateTime? = null,
    val expiredAt: LocalDateTime? = null,
) {
    fun toDomain(): Job =
        Job(
            jobId = this.jobId,
            userId = this.userId,
            jobName = this.jobName,
            status = JobStatus.valueOf(this.status),
            zipUrl = this.zipUrl,
            originalSize = this.originalSize,
            convertedSize = this.convertedSize,
            imageCount = this.imageCount,
            doneCount = this.doneCount,
            createdAt = this.createdAt,
            finishedAt = this.finishedAt,
            expiredAt = this.expiredAt,
        )

    companion object {
        fun from(domain: Job): JobEntity =
            JobEntity(
                jobId = domain.jobId,
                userId = domain.userId,
                jobName = domain.jobName,
                status = domain.status.name,
                zipUrl = domain.zipUrl,
                originalSize = domain.originalSize,
                convertedSize = domain.convertedSize,
                imageCount = domain.imageCount,
                doneCount = domain.doneCount,
                createdAt = domain.createdAt,
                finishedAt = domain.finishedAt,
                expiredAt = domain.expiredAt,
            )
    }
}
