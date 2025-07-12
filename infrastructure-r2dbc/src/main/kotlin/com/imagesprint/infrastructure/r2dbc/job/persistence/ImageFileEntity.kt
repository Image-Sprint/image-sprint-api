package com.imagesprint.infrastructure.r2dbc.job.persistence

import com.imagesprint.core.domain.job.ImageFile
import com.imagesprint.core.port.input.job.ConvertStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("image_file")
data class ImageFileEntity(
    @Id
    val imageFileId: Long? = null,
    val jobId: Long,
    val fileName: String,
    val format: String,
    val size: Long,
    val convertedSize: Long? = null,
    val convertStatus: String,
    val errorMessage: String? = null,
    val createdAt: LocalDateTime,
) {
    fun toDomain(): ImageFile =
        ImageFile(
            imageFileId = this.imageFileId,
            jobId = this.jobId,
            fileName = this.fileName,
            format = this.format,
            size = this.size,
            convertedSize = this.convertedSize,
            convertStatus = ConvertStatus.valueOf(this.convertStatus),
            errorMessage = this.errorMessage,
            createdAt = this.createdAt,
        )

    companion object {
        fun from(domain: ImageFile): ImageFileEntity =
            ImageFileEntity(
                imageFileId = domain.imageFileId,
                jobId = domain.jobId!!,
                fileName = domain.fileName,
                format = domain.format,
                size = domain.size,
                convertedSize = domain.convertedSize,
                convertStatus = domain.convertStatus.name,
                errorMessage = domain.errorMessage,
                createdAt = domain.createdAt,
            )
    }
}
