package com.imagesprint.core.support.factory

import com.imagesprint.core.port.input.job.CreateJobCommand
import com.imagesprint.core.port.input.job.ImageUploadMeta
import com.imagesprint.core.port.input.job.WatermarkPosition
import java.io.ByteArrayInputStream

object CreateJobCommandTestFactory {
    fun valid(): CreateJobCommand =
        CreateJobCommand(
            userId = 1L,
            fileMetas =
                listOf(
                    ImageUploadMeta(
                        "image1.png",
                        12345,
                        "image/png",
                        ByteArrayInputStream(ByteArray(20)),
                    ),
                    ImageUploadMeta(
                        "image2.jpg",
                        23456,
                        "image/jpeg",
                        ByteArrayInputStream(ByteArray(20)),
                    ),
                ),
            resizeWidth = 800,
            resizeHeight = 600,
            keepRatio = true,
            toFormat = "jpeg",
            quality = 80,
            watermarkText = "sample",
            watermarkPosition = WatermarkPosition.BOTTOM_RIGHT,
            watermarkOpacity = 0.5f,
        )

    fun withQuality(quality: Int): CreateJobCommand = valid().copy(quality = quality)
}
