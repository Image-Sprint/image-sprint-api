package com.imagesprint.core.port.output.job

import com.imagesprint.core.domain.job.ImageFile

interface ReactiveImageRepository {
    suspend fun save(imageFile: ImageFile): ImageFile

    suspend fun updateConvertedSize(
        imageFileId: Long,
        convertedSize: Long,
    )

    suspend fun deleteAll()

    suspend fun getAllForJob(jobId: Long): List<ImageFile>
}
