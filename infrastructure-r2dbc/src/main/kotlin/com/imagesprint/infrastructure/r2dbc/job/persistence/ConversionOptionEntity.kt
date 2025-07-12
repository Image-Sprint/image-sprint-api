package com.imagesprint.infrastructure.r2dbc.job.persistence

import com.imagesprint.core.domain.job.ConversionOption
import com.imagesprint.core.port.input.job.WatermarkPosition
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("conversion_option")
data class ConversionOptionEntity(
    @Id
    val conversionOptionId: Long? = null,
    val jobId: Long,
    val resizeWidth: Int,
    val resizeHeight: Int,
    val keepRatio: Boolean,
    val toFormat: String,
    val quality: Int,
    val watermarkText: String,
    val watermarkPosition: String,
    val watermarkOpacity: Float,
) {
    fun toDomain(): ConversionOption =
        ConversionOption(
            conversionOptionId = this.conversionOptionId,
            jobId = this.jobId,
            resizeWidth = this.resizeWidth,
            resizeHeight = this.resizeHeight,
            keepRatio = this.keepRatio,
            toFormat = this.toFormat,
            quality = this.quality,
            watermarkText = this.watermarkText,
            watermarkPosition = WatermarkPosition.valueOf(this.watermarkPosition),
            watermarkOpacity = this.watermarkOpacity,
        )

    companion object {
        fun from(domain: ConversionOption): ConversionOptionEntity =
            ConversionOptionEntity(
                conversionOptionId = domain.conversionOptionId,
                jobId = domain.jobId,
                resizeWidth = domain.resizeWidth!!,
                resizeHeight = domain.resizeHeight!!,
                keepRatio = domain.keepRatio,
                toFormat = domain.toFormat,
                quality = domain.quality,
                watermarkText = domain.watermarkText!!,
                watermarkPosition = domain.watermarkPosition!!.name,
                watermarkOpacity = domain.watermarkOpacity!!,
            )
    }
}
