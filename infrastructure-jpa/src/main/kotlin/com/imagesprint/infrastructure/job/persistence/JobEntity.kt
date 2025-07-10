package com.imagesprint.infrastructure.job.persistence

import com.imagesprint.core.domain.job.Job
import com.imagesprint.core.port.input.job.JobStatus
import com.imagesprint.infrastructure.common.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "job")
class JobEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val jobId: Long? = null,
    @Column(nullable = false)
    val userId: Long,
    @Column(nullable = false)
    val jobName: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: JobStatus,
    @Column(nullable = false)
    val originalSize: Long,
    @Column(nullable = false)
    val imageCount: Int,
    @Column(columnDefinition = "TEXT")
    var zipUrl: String? = null,
    var convertedSize: Long? = null,
    var doneCount: Int? = null,
    var finishedAt: LocalDateTime? = null,
    var expiredAt: LocalDateTime? = null,
) : BaseTimeEntity() {
    fun toDomain(): Job =
        Job(
            jobId = jobId,
            userId = userId,
            jobName = jobName,
            zipUrl = zipUrl,
            status = status,
            originalSize = originalSize,
            convertedSize = convertedSize,
            imageCount = imageCount,
            doneCount = doneCount,
            createdAt = createdAt,
            finishedAt = finishedAt,
            expiredAt = expiredAt,
        )

    companion object {
        fun from(job: Job): JobEntity =
            JobEntity(
                jobId = job.jobId,
                userId = job.userId,
                jobName = job.jobName,
                status = job.status,
                originalSize = job.originalSize,
                imageCount = job.imageCount,
                zipUrl = job.zipUrl,
                convertedSize = job.convertedSize,
                doneCount = job.doneCount,
                finishedAt = job.finishedAt,
                expiredAt = job.expiredAt,
            )
    }
}
