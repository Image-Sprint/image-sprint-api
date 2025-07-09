package com.imagesprint.workerserver.support

import com.imagesprint.core.domain.job.ConversionOption
import com.imagesprint.core.domain.job.ImageFile
import com.imagesprint.core.domain.job.Job
import com.imagesprint.core.port.input.job.ConvertStatus
import com.imagesprint.core.port.input.job.JobStatus
import com.imagesprint.core.port.input.job.WatermarkPosition
import java.time.LocalDateTime

object TestEntityFactory {
    fun job(
        jobId: Long? = null,
        userId: Long = 1L,
        status: JobStatus = JobStatus.PENDING,
        imageCount: Int = 1,
        originalSize: Long = 1024L,
        createdAt: LocalDateTime = LocalDateTime.now(),
    ) = Job(
        jobId = jobId, // null로 전달되어야 DB에서 auto-increment됨
        userId = userId,
        jobName = "Test Job",
        status = status,
        originalSize = originalSize,
        imageCount = imageCount,
        createdAt = createdAt,
    )

    fun imageFile(
        jobId: Long,
        fileName: String = "sample.jpg",
        format: String = "jpg",
        size: Long = 1024L,
    ) = ImageFile(
        imageFileId = null, // 생성 시 null
        jobId = jobId,
        fileName = fileName,
        format = format,
        size = size,
        convertedSize = null,
        convertStatus = ConvertStatus.WAITING,
        errorMessage = null,
        createdAt = LocalDateTime.now(),
    )

    fun conversionOption(jobId: Long) =
        ConversionOption(
            conversionOptionId = null, // 생성 시 null
            jobId = jobId,
            resizeWidth = 300,
            resizeHeight = 300,
            keepRatio = true,
            toFormat = "png",
            quality = 80,
            watermarkText = "Watermark",
            watermarkPosition = WatermarkPosition.BOTTOM_RIGHT,
            watermarkOpacity = 0.5f,
        )
}
