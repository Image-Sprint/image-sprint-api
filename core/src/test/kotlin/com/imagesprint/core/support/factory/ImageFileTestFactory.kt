package com.imagesprint.core.support.factory

import com.imagesprint.core.domain.job.ImageFile
import com.imagesprint.core.port.input.job.ConvertStatus
import java.time.LocalDateTime

object ImageFileTestFactory {
    fun create(
        imageFileId: Long? = 1L,
        jobId: Long? = null,
        fileName: String = "sample.jpg",
        format: String = "jpeg",
        size: Long = 12345,
        convertedSize: Long? = null,
        convertStatus: ConvertStatus = ConvertStatus.WAITING,
        errorMessage: String? = null,
        createdAt: LocalDateTime = LocalDateTime.now(),
    ): ImageFile =
        ImageFile(
            imageFileId = imageFileId,
            jobId = jobId,
            fileName = fileName,
            format = format,
            size = size,
            convertedSize = convertedSize,
            convertStatus = convertStatus,
            errorMessage = errorMessage,
            createdAt = createdAt,
        )
}
