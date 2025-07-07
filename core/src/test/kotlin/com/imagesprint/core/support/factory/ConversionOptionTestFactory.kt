package com.imagesprint.core.support.factory

import com.imagesprint.core.domain.job.ConversionOption
import com.imagesprint.core.port.input.job.WatermarkPosition

object ConversionOptionTestFactory {
    fun create(
        jobId: Long = 1L,
        resizeWidth: Int? = 800,
        resizeHeight: Int? = 600,
        keepRatio: Boolean = true,
        toFormat: String = "jpeg",
        quality: Int = 80,
        watermarkText: String? = "Sample Watermark",
        watermarkPosition: WatermarkPosition? = WatermarkPosition.BOTTOM_RIGHT,
        watermarkOpacity: Float? = 0.5f,
    ): ConversionOption =
        ConversionOption(
            jobId = jobId,
            resizeWidth = resizeWidth,
            resizeHeight = resizeHeight,
            keepRatio = keepRatio,
            toFormat = toFormat,
            quality = quality,
            watermarkText = watermarkText,
            watermarkPosition = watermarkPosition,
            watermarkOpacity = watermarkOpacity,
        )
}
