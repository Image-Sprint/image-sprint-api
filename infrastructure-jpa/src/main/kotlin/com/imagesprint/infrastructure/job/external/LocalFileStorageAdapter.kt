package com.imagesprint.infrastructure.job.external

import com.imagesprint.core.port.input.job.ImageUploadMeta
import com.imagesprint.core.port.input.job.SavedImageMeta
import com.imagesprint.core.port.output.job.FileStoragePort
import org.springframework.stereotype.Component
import java.io.File

@Component
class LocalFileStorageAdapter : FileStoragePort {
    override fun saveOriginalFiles(
        jobId: Long,
        files: List<ImageUploadMeta>,
        savedImages: List<SavedImageMeta>,
    ) {
        files.zip(savedImages).forEach { (meta, saved) ->
            val targetFile =
                File(
                    System.getProperty("java.io.tmpdir"),
                    "$jobId/${saved.imageFileId}_${meta.originalFilename}",
                )
            targetFile.parentFile.mkdirs()
            meta.inputStream.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}
