package com.imagesprint.core.domain.job

import com.imagesprint.core.port.input.job.WatermarkPosition

data class ConversionOption(
    val conversionOptionId: Long? = null,
    val jobId: Long,
    val resizeWidth: Int?,
    val resizeHeight: Int?,
    val keepRatio: Boolean,
    val toFormat: String,
    val quality: Int,
    val watermarkText: String?,
    val watermarkPosition: WatermarkPosition?,
    val watermarkOpacity: Float?,
)
