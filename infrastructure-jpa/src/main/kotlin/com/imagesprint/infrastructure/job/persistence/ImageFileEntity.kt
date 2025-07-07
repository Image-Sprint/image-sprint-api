package com.imagesprint.infrastructure.job.persistence

import com.imagesprint.core.domain.job.ImageFile
import com.imagesprint.core.port.input.job.ConvertStatus
import com.imagesprint.infrastructure.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "image_file")
class ImageFileEntity(
    job: JobEntity?,
    fileName: String,
    format: String,
    size: Long,
    convertStatus: ConvertStatus,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val imageFileId: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    var job: JobEntity? = job
        protected set

    @Column(nullable = false)
    var fileName: String = fileName
        protected set

    @Column(nullable = false)
    var format: String = format
        protected set

    @Column(nullable = false)
    var size: Long = size
        protected set

    var convertedSize: Long? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var convertStatus: ConvertStatus = convertStatus
        protected set

    var errorMessage: String? = null

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
                job = job,
                fileName = file.fileName,
                format = file.format,
                size = file.size,
                convertStatus = file.convertStatus,
            ).apply {
                this.convertedSize = file.convertedSize
                this.errorMessage = file.errorMessage
            }
    }
}
