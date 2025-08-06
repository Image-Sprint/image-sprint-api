package com.imagesprint.infrastructure.r2dbc.job.persistence

import com.imagesprint.core.domain.job.Job
import com.imagesprint.core.port.input.job.JobStatus
import com.imagesprint.core.port.output.job.ReactiveJobRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ReactiveJobRepositoryImpl(
    private val client: DatabaseClient,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : ReactiveJobRepository {
    override suspend fun save(job: Job): Job {
        val entity = JobEntity.from(job)

        val inserted: JobEntity =
            r2dbcEntityTemplate
                .insert(JobEntity::class.java)
                .using(entity)
                .awaitFirstOrNull()
                ?: error("Failed to insert JobEntity: $entity")

        return inserted.toDomain()
    }

    override suspend fun deleteAll() {
        client.sql("DELETE FROM job").then().awaitFirstOrNull()
    }

    override suspend fun getJob(jobId: Long): Job? =
        client
            .sql("SELECT * FROM job WHERE job_id = ?")
            .bind(0, jobId)
            .map { row, _ -> JobRow.from(row).toDomain() }
            .one()
            .awaitFirstOrNull()

    override suspend fun incrementDoneCount(jobId: Long) {
        client
            .sql(
                """
                UPDATE job
                SET done_count = COALESCE(done_count, 0) + 1
                WHERE job_id = :jobId
                """.trimIndent(),
            ).bind("jobId", jobId)
            .fetch()
            .rowsUpdated()
            .awaitFirstOrNull()
    }

    override suspend fun updateStatus(
        jobId: Long,
        status: JobStatus,
        startedAt: LocalDateTime,
    ) {
        client
            .sql(
                """
                UPDATE `job`
                SET status = :status,
                    created_at = :startedAt
                WHERE job_id = :jobId
                """.trimIndent(),
            ).bind("status", status.name)
            .bind("startedAt", startedAt)
            .bind("jobId", jobId)
            .fetch()
            .rowsUpdated()
            .awaitFirstOrNull()
    }

    override suspend fun updateSummary(
        jobId: Long,
        status: JobStatus,
        doneCount: Int,
        convertedSize: Long,
        finishedAt: LocalDateTime,
        expiredAt: LocalDateTime?,
        zipUrl: String,
    ) {
        client
            .sql(
                """
                UPDATE `job`
                SET status = :status,
                    done_count = :doneCount,
                    converted_size = :convertedSize,
                    finished_at = :finishedAt,
                    expired_at = :expiredAt,
                    zip_url = :zipUrl
                WHERE job_id = :jobId
                """.trimIndent(),
            ).bind("status", status.name)
            .bind("doneCount", doneCount)
            .bind("convertedSize", convertedSize)
            .bind("finishedAt", finishedAt)
            .bind("expiredAt", expiredAt ?: LocalDateTime.now())
            .bind("jobId", jobId)
            .bind("zipUrl", zipUrl)
            .fetch()
            .rowsUpdated()
            .awaitFirstOrNull()
    }

    fun JobRow.toDomain(): Job =
        Job(
            jobId = this.jobId,
            userId = this.userId,
            jobName = this.jobName,
            status = JobStatus.valueOf(this.status),
            originalSize = this.originalSize,
            convertedSize = this.convertedSize,
            imageCount = this.imageCount,
            doneCount = this.doneCount,
            createdAt = this.createdAt,
            finishedAt = this.finishedAt,
            expiredAt = this.expiredAt,
        )
}
