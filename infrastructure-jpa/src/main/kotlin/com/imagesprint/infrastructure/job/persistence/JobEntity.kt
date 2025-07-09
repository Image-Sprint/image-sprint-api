package com.imagesprint.infrastructure.job.persistence

import com.imagesprint.core.domain.job.Job
import com.imagesprint.core.port.input.job.JobStatus
import com.imagesprint.infrastructure.common.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "job")
class JobEntity(
    userId: Long,
    jobName: String,
    status: JobStatus,
    originalSize: Long,
    imageCount: Int,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val jobId: Long? = null

    @Column(nullable = false)
    var userId: Long = userId
        protected set

    @Column(nullable = false)
    var jobName: String = jobName
        protected set

    @Column
    var zipUrl: String? = null
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: JobStatus = status
        protected set

    @Column(nullable = false)
    var originalSize: Long = originalSize
        protected set

    @Column(nullable = false)
    var imageCount: Int = imageCount
        protected set

    var convertedSize: Long? = null
    var doneCount: Int? = null
    var finishedAt: LocalDateTime? = null
    var expiredAt: LocalDateTime? = null

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
                userId = job.userId,
                jobName = job.jobName,
                status = job.status,
                originalSize = job.originalSize,
                imageCount = job.imageCount,
            ).apply {
                this.zipUrl = job.zipUrl
                this.convertedSize = job.convertedSize
                this.doneCount = job.doneCount
                this.finishedAt = job.finishedAt
                this.expiredAt = job.expiredAt
            }
    }
}
