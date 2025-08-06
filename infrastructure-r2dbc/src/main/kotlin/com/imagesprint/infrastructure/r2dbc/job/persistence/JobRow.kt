package com.imagesprint.infrastructure.r2dbc.job.persistence

import io.r2dbc.spi.Row
import java.time.LocalDateTime

data class JobRow(
    val jobId: Long,
    val userId: Long,
    val jobName: String,
    val status: String,
    val originalSize: Long,
    val convertedSize: Long? = null,
    val imageCount: Int,
    val doneCount: Int? = null,
    val createdAt: LocalDateTime,
    val finishedAt: LocalDateTime? = null,
    val expiredAt: LocalDateTime? = null,
) {
    companion object {
        fun from(row: Row): JobRow =
            JobRow(
                jobId = row.get("job_id", java.lang.Long::class.java)!!.toLong(),
                userId = row.get("user_id", java.lang.Long::class.java)!!.toLong(),
                jobName = row.get("job_name", String::class.java)!!,
                status = row.get("status", String::class.java)!!,
                originalSize = row.get("original_size", java.lang.Long::class.java)!!.toLong(),
                convertedSize = row.get("converted_size", java.lang.Long::class.java)?.toLong(),
                imageCount = row.get("image_count", Integer::class.java)!!.toInt(),
                doneCount = row.get("done_count", Integer::class.java)?.toInt(),
                createdAt = row.get("created_at", LocalDateTime::class.java)!!,
                finishedAt = row.get("finished_at", LocalDateTime::class.java),
                expiredAt = row.get("expired_at", LocalDateTime::class.java),
            )
    }
}
