package com.imagesprint.core.port.output.job

import com.imagesprint.core.domain.job.ImageFile

interface ImageRepository {
    fun saveImages(images: List<ImageFile>): List<ImageFile>

    fun updateJobIdAndStatus(
        jobId: Long,
        imageIds: List<Long>,
    )
}
