package com.imagesprint.infrastructure.job.external

import com.imagesprint.core.port.input.job.ImageUploadMeta
import com.imagesprint.core.port.input.job.SavedImageMeta
import com.imagesprint.core.port.output.job.FileStoragePort
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths

@Component
class LocalFileStorageAdapter : FileStoragePort {
    override fun saveOriginalFiles(
        userId: Long,
        files: List<ImageUploadMeta>,
        savedImages: List<SavedImageMeta>,
    ) {
        val baseDir = Paths.get("tmp", "uploads", userId.toString())
        Files.createDirectories(baseDir)

        files.zip(savedImages).forEach { (meta, saved) ->
            val filename = "${saved.imageFileId}_${meta.originalFilename}"
            val filePath = baseDir.resolve(filename)
            Files.write(filePath, meta.bytes)
        }
    }
}
