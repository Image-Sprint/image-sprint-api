package com.imagesprint.infrastructure.r2dbc.job.persistence

import com.imagesprint.core.domain.job.ConversionOption
import com.imagesprint.core.port.input.job.WatermarkPosition
import com.imagesprint.core.port.output.job.ReactiveConversionOptionRepository
import io.r2dbc.spi.Row
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class ReactiveConversionOptionRepositoryImpl(
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
    private val client: DatabaseClient,
) : ReactiveConversionOptionRepository {
    override suspend fun save(option: ConversionOption): ConversionOption {
        val entity = ConversionOptionEntity.from(option)

        val inserted =
            r2dbcEntityTemplate
                .insert(ConversionOptionEntity::class.java)
                .using(entity)
                .awaitFirstOrNull()
                ?: error("Failed to insert ConversionOptionEntity: $entity")

        return inserted.toDomain()
    }

    override suspend fun deleteAll() {
        client.sql("DELETE FROM conversion_option").then().awaitFirstOrNull()
    }

    override suspend fun getForJob(jobId: Long): ConversionOption =
        client
            .sql("SELECT * FROM conversion_option WHERE job_id = ?")
            .bind(0, jobId)
            .map { row, _ -> fromRow(row) }
            .one()
            .awaitSingleOrNull() ?: throw IllegalStateException("ConversionOption not found for jobId=$jobId")

    private fun fromRow(row: Row): ConversionOption =
        ConversionOption(
            conversionOptionId = row.get("conversion_option_id", java.lang.Long::class.java)!!.toLong(),
            jobId = row.get("job_id", java.lang.Long::class.java)!!.toLong(),
            resizeWidth = row.get("resize_width", Integer::class.java)?.toInt(),
            resizeHeight = row.get("resize_height", Integer::class.java)?.toInt(),
            keepRatio = (row.get("keep_ratio", java.lang.Boolean::class.java) as Boolean?) ?: true,
            toFormat = row.get("to_format", String::class.java)!!,
            quality = row.get("quality", Integer::class.java)?.toInt()!!,
            watermarkText = row.get("watermark_text", String::class.java),
            watermarkPosition =
                row
                    .get("watermark_position", String::class.java)
                    ?.let { WatermarkPosition.valueOf(it) },
            watermarkOpacity = row.get("watermark_opacity", Float::class.java)?.toFloat(),
        )
}
