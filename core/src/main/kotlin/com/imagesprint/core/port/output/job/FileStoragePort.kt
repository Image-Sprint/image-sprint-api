package com.imagesprint.core.port.output.job

import com.imagesprint.core.port.input.job.ImageUploadMeta
import com.imagesprint.core.port.input.job.SavedImageMeta

interface FileStoragePort {
    fun saveOriginalFiles(
        jobId: Long,
        files: List<ImageUploadMeta>,
        savedImages: List<SavedImageMeta>,
    )
}
