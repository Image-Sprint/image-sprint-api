package com.imagesprint.workerserver.processor

import com.imagesprint.core.domain.job.ConversionOption
import com.imagesprint.core.domain.job.ImageFile
import com.imagesprint.core.port.input.job.ConvertStatus
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import java.io.File
import java.time.LocalDateTime
import kotlin.test.Test

class ImageProcessorTest {
    private lateinit var imageProcessor: ImageProcessor

    @BeforeEach
    fun setUp() {
        imageProcessor = ImageProcessor()
    }

    @Test
    fun `단위 - 이미지 변환 성공 시 파일 크기를 반환한다`() =
        runBlocking {
            // given
            val jobId = 1L
            val imageFileId = 100L
            val fileName = "sample.jpg"
            val sourceFile = copySampleImageToTemp(jobId, imageFileId, fileName)

            val image =
                ImageFile(
                    imageFileId = imageFileId,
                    jobId = jobId,
                    fileName = fileName,
                    format = "jpg",
                    size = sourceFile.length(),
                    convertedSize = null,
                    convertStatus = ConvertStatus.WAITING,
                    errorMessage = null,
                    createdAt = LocalDateTime.now(),
                )

            val option =
                ConversionOption(
                    conversionOptionId = null,
                    jobId = jobId,
                    resizeWidth = 300,
                    resizeHeight = 300,
                    keepRatio = true,
                    toFormat = "jpg",
                    quality = 90,
                    watermarkText = null,
                    watermarkPosition = null,
                    watermarkOpacity = null,
                )

            // when
            val result = imageProcessor.processImage(jobId, image, option)

            // then
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()).isGreaterThan(0L)

            imageProcessor.cleanTempFiles(jobId)
        }

    @Test
    fun `단위 - 존재하지 않는 원본 파일일 경우 예외가 발생한다`() {
        runBlocking {
            // given
            val jobId = 2L
            val imageFileId = 200L
            val image =
                ImageFile(
                    imageFileId = imageFileId,
                    jobId = jobId,
                    fileName = "nonexistent.jpg",
                    format = "jpg",
                    size = 0L,
                    convertedSize = null,
                    convertStatus = ConvertStatus.WAITING,
                    errorMessage = null,
                    createdAt = LocalDateTime.now(),
                )

            val option =
                ConversionOption(
                    conversionOptionId = null,
                    jobId = jobId,
                    resizeWidth = 300,
                    resizeHeight = 300,
                    keepRatio = true,
                    toFormat = "jpg",
                    quality = 90,
                    watermarkText = null,
                    watermarkPosition = null,
                    watermarkOpacity = null,
                )

            // when
            val result = imageProcessor.processImage(jobId, image, option)

            // then
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(java.io.FileNotFoundException::class.java)
        }
    }

    /**
     * 테스트에 사용할 sample.jpg 파일을 /tmp 디렉토리로 복사
     */
    private fun copySampleImageToTemp(
        jobId: Long,
        imageFileId: Long,
        fileName: String,
    ): File {
        val source = File("src/test/resources/$fileName")
        val baseDir = File(System.getProperty("java.io.tmpdir"), jobId.toString())
        baseDir.mkdirs()
        val dest = File(baseDir, "${imageFileId}_$fileName")
        source.copyTo(dest, overwrite = true)
        return dest
    }
}
