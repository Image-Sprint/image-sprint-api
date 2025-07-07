package com.imagesprint.infrastructure.job.persistence

import com.imagesprint.core.domain.job.ConversionOption
import com.imagesprint.core.port.input.job.WatermarkPosition
import com.imagesprint.infrastructure.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "conversion_option")
class ConversionOptionEntity(
    job: JobEntity,
    resizeWidth: Int?,
    resizeHeight: Int?,
    keepRatio: Boolean,
    toFormat: String,
    quality: Int,
    watermarkText: String?,
    watermarkPosition: WatermarkPosition?,
    watermarkOpacity: Float?,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val conversionOptionId: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", unique = true, nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    var job: JobEntity = job
        protected set

    var resizeWidth: Int? = resizeWidth
        protected set

    var resizeHeight: Int? = resizeHeight
        protected set

    @Column(nullable = false)
    var keepRatio: Boolean = keepRatio
        protected set

    @Column(nullable = false)
    var toFormat: String = toFormat
        protected set

    @Column(nullable = false)
    var quality: Int = quality
        protected set

    var watermarkText: String? = watermarkText
        protected set

    @Enumerated(EnumType.STRING)
    var watermarkPosition: WatermarkPosition? = watermarkPosition
        protected set

    var watermarkOpacity: Float? = watermarkOpacity
        protected set

    fun toDomain(): ConversionOption =
        ConversionOption(
            jobId = job.jobId!!,
            resizeWidth = resizeWidth,
            resizeHeight = resizeHeight,
            keepRatio = keepRatio,
            toFormat = toFormat,
            quality = quality,
            watermarkText = watermarkText,
            watermarkPosition = watermarkPosition,
            watermarkOpacity = watermarkOpacity,
        )

    companion object {
        fun from(
            option: ConversionOption,
            job: JobEntity,
        ): ConversionOptionEntity =
            ConversionOptionEntity(
                job = job,
                resizeWidth = option.resizeWidth,
                resizeHeight = option.resizeHeight,
                keepRatio = option.keepRatio,
                toFormat = option.toFormat,
                quality = option.quality,
                watermarkText = option.watermarkText,
                watermarkPosition = option.watermarkPosition,
                watermarkOpacity = option.watermarkOpacity,
            )
    }
}
