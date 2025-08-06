package com.imagesprint.core.port.input.job

import java.io.InputStream

data class ImageUploadMeta(
    val originalFilename: String,
    val size: Long,
    val contentType: String,
    val inputStream: InputStream,
)
