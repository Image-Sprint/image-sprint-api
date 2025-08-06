package com.imagesprint.apiserver.controller.job.dto

import com.imagesprint.core.port.input.job.CreateJobCommand
import com.imagesprint.core.port.input.job.ImageUploadMeta
import com.imagesprint.core.port.input.job.WatermarkPosition
import jakarta.validation.constraints.*

data class CreateJobOptionRequest(
    @Min(1)
    val resizeWidth: Int? = 1000,
    @Min(1)
    val resizeHeight: Int? = 800,
    val keepRatio: Boolean = true,
    @NotBlank
    val toFormat: String,
    @Min(1)
    @Max(100)
    val quality: Int = 80,
    val watermarkText: String?,
    val watermarkPosition: WatermarkPosition?,
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    val watermarkOpacity: Float?,
) {
    fun toCommand(
        userId: Long,
        fileMetas: List<ImageUploadMeta>,
    ): CreateJobCommand =
        CreateJobCommand(
            userId = userId,
            fileMetas = fileMetas,
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
