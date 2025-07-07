package com.imagesprint.core.port.input.job

data class ImageUploadMeta(
    val originalFilename: String,
    val size: Long,
    val contentType: String,
    val bytes: ByteArray, // 또는 InputStream
)
