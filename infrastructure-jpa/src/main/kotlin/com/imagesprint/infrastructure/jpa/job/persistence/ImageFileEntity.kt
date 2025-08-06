package com.imagesprint.infrastructure.jpa.job.persistence

import com.imagesprint.core.domain.job.ImageFile
import com.imagesprint.core.port.input.job.ConvertStatus
import com.imagesprint.infrastructure.jpa.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "image_file")
class ImageFileEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val imageFileId: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val job: JobEntity? = null,
    @Column(nullable = false)
    val fileName: String,
    @Column(nullable = false)
    val format: String,
    @Column(nullable = false)
    val size: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val convertStatus: ConvertStatus,
    var convertedSize: Long? = null,
    var errorMessage: String? = null,
) : BaseTimeEntity() {
    fun toDomain(): ImageFile =
        ImageFile(
            imageFileId = imageFileId,
            jobId = job?.jobId,
            fileName = fileName,
            format = format,
            size = size,
            convertedSize = convertedSize,
            convertStatus = convertStatus,
            errorMessage = errorMessage,
            createdAt = createdAt,
        )

    companion object {
        fun from(
            file: ImageFile,
            job: JobEntity? = null,
        ): ImageFileEntity =
            ImageFileEntity(
                imageFileId = file.imageFileId,
                job = job,
                fileName = file.fileName,
                format = file.format,
                size = file.size,
                convertStatus = file.convertStatus,
                convertedSize = file.convertedSize,
                errorMessage = file.errorMessage,
            )
    }
}
