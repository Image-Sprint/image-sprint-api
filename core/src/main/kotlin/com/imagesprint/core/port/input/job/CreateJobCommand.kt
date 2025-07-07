package com.imagesprint.core.port.input.job

import com.imagesprint.core.exception.CustomException
import com.imagesprint.core.exception.ErrorCode

data class CreateJobCommand(
    val userId: Long,
    val fileMetas: List<ImageUploadMeta>,
    val resizeWidth: Int?,
    val resizeHeight: Int?,
    val keepRatio: Boolean,
    val toFormat: String,
    val quality: Int,
    val watermarkText: String?,
    val watermarkPosition: WatermarkPosition?,
    val watermarkOpacity: Float?,
) {
    fun validate() {
        if (quality !in 1..100) throw CustomException(ErrorCode.INVALID_QUALITY)
        if (resizeWidth != null && resizeWidth <= 0) throw CustomException(ErrorCode.INVALID_RESIZE_WIDTH)
        if (resizeHeight != null && resizeHeight <= 0) throw CustomException(ErrorCode.INVALID_RESIZE_HEIGHT)
    }
}
