package com.imagesprint.infrastructure.job.persistence

import com.imagesprint.core.domain.job.ConversionOption
import com.imagesprint.core.port.input.job.WatermarkPosition
import com.imagesprint.infrastructure.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "conversion_option")
class ConversionOptionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val conversionOptionId: Long? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", unique = true, nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val job: JobEntity,
    val resizeWidth: Int? = null,
    val resizeHeight: Int? = null,
    @Column(nullable = false)
    val keepRatio: Boolean,
    @Column(nullable = false)
    val toFormat: String,
    @Column(nullable = false)
    val quality: Int,
    val watermarkText: String? = null,
    @Enumerated(EnumType.STRING)
    val watermarkPosition: WatermarkPosition? = null,
    val watermarkOpacity: Float? = null,
) : BaseTimeEntity() {
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
                conversionOptionId = null, // 생성 시에는 null
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
