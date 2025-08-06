package com.imagesprint.infrastructure.r2dbc.job.persistence

import com.imagesprint.core.domain.job.ImageFile
import com.imagesprint.core.port.input.job.ConvertStatus
import com.imagesprint.core.port.output.job.ReactiveImageRepository
import io.r2dbc.spi.Row
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ReactiveImageRepositoryImpl(
    private val client: DatabaseClient,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : ReactiveImageRepository {
    override suspend fun save(imageFile: ImageFile): ImageFile {
        val entity = ImageFileEntity.from(imageFile)

        val inserted =
            r2dbcEntityTemplate
                .insert(ImageFileEntity::class.java)
                .using(entity)
                .awaitFirstOrNull()
                ?: error("❌ Failed to insert ImageFileEntity: $entity")

        return inserted.toDomain()
    }

    override suspend fun updateConvertedSize(
        imageFileId: Long,
        convertedSize: Long,
    ) {
        client
            .sql(
                """
                UPDATE image_file
                SET converted_size = :convertedSize
                WHERE image_file_id = :imageFileId
                """.trimIndent(),
            ).bind("convertedSize", convertedSize)
            .bind("imageFileId", imageFileId)
            .fetch()
            .rowsUpdated()
            .awaitSingleOrNull()
    }

    override suspend fun deleteAll() {
        client.sql("DELETE FROM image_file").then().awaitFirstOrNull()
    }

    override suspend fun getAllForJob(jobId: Long): List<ImageFile> =
        client
            .sql("SELECT * FROM image_file WHERE job_id = ?")
            .bind(0, jobId)
            .map { row, _ -> fromRow(row) }
            .all()
            .collectList()
            .awaitSingle()

    private fun fromRow(row: Row): ImageFile {
        val imageFileId =
            requireNotNull(row.get("image_file_id", java.lang.Long::class.java)) {
                "❌ image_file_id is null"
            }.toLong()

        val jobId =
            requireNotNull(row.get("job_id", java.lang.Long::class.java)) {
                "❌ job_id is null"
            }.toLong()

        val fileName =
            requireNotNull(row.get("file_name", String::class.java)) {
                "❌ file_name is null"
            }

        val size =
            requireNotNull(row.get("size", java.lang.Long::class.java)) {
                "❌ size is null"
            }.toLong()

        val convertedSize = row.get("converted_size", java.lang.Long::class.java)?.toLong()

        val format =
            requireNotNull(row.get("format", String::class.java)) {
                "❌ format is null"
            }

        val convertStatus =
            ConvertStatus.valueOf(
                requireNotNull(row.get("convert_status", String::class.java)) {
                    "❌ convert_status is null"
                },
            )

        val errorMessage = row.get("error_message", String::class.java)

        val createdAt =
            requireNotNull(row.get("created_at", LocalDateTime::class.java)) {
                "❌ created_at is null"
            }

        return ImageFile(
            imageFileId = imageFileId,
            jobId = jobId,
            fileName = fileName,
            size = size,
            convertedSize = convertedSize,
            format = format,
            convertStatus = convertStatus,
            errorMessage = errorMessage,
            createdAt = createdAt,
        )
    }
}
