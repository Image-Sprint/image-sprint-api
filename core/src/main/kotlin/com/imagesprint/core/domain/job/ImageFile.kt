package com.imagesprint.core.domain.job

import com.imagesprint.core.port.input.job.ConvertStatus
import java.time.LocalDateTime

data class ImageFile(
    val imageFileId: Long? = null,
    val jobId: Long?,
    val fileName: String,
    val format: String,
    val size: Long,
    val convertedSize: Long?,
    val convertStatus: ConvertStatus,
    val errorMessage: String?,
    val createdAt: LocalDateTime,
)
