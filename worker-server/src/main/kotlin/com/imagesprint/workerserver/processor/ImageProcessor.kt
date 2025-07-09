package com.imagesprint.workerserver.processor

import com.imagesprint.core.domain.job.ConversionOption
import com.imagesprint.core.domain.job.ImageFile
import com.imagesprint.core.port.input.job.WatermarkPosition
import net.coobird.thumbnailator.Thumbnails
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.imageio.ImageIO

/**
 * 주어진 원본 이미지에 대해 리사이징, 압축, 포맷 변환, 워터마크 적용 등의 변환 작업을 수행하고,
 * 최종 결과 파일을 `/tmp/{jobId}/converted/` 디렉토리에 저장한 뒤, 변환된 파일의 크기를 반환합니다.
 *
 * 주요 처리 과정:
 * 1. 원본 파일 경로 및 결과 파일 경로 설정
 * 2. Thumbnailator를 사용한 이미지 리사이징 및 포맷 변환
 * 3. 워터마크 텍스트가 있을 경우 텍스트 워터마크 적용
 * 4. 최종 변환 이미지 파일 생성 및 임시 파일 정리
 * 5. 최종 파일의 바이트 크기를 Result.success로 반환
 *
 * 실패 시 예외 메시지를 포함한 Result.failure 반환
 *
 * @param jobId 변환 작업 ID
 * @param image 변환 대상 이미지 정보
 * @param option 변환 옵션 (리사이즈, 포맷, 워터마크 등)
 * @return 변환된 이미지 파일 크기 (bytes) 또는 실패 시 예외
 */
@Component
class ImageProcessor {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun processImage(
        jobId: Long,
        image: ImageFile,
        option: ConversionOption,
    ): Result<Long> =
        try {
            // 1. 소스 및 타겟 경로 구성
            val sourcePath = "/tmp/$jobId/${image.imageFileId}__${image.fileName}"
            val targetDir = File("/tmp/$jobId/converted")
            targetDir.mkdirs()

            val fileNameBase = image.imageFileId.toString() + "__" + image.fileName.substringBeforeLast('.')
            val fileExt = option.toFormat
            val tempFile = File(targetDir, "${fileNameBase}_tmp.$fileExt")
            val finalFile = File(targetDir, "$fileNameBase.$fileExt")

            // 2. 썸네일 생성
            val builder =
                Thumbnails.of(File(sourcePath)).apply {
                    if (option.keepRatio) {
                        size(requireNotNull(option.resizeWidth), requireNotNull(option.resizeHeight))
                    } else {
                        forceSize(requireNotNull(option.resizeWidth), requireNotNull(option.resizeHeight))
                    }
                    outputQuality(option.quality / 100.0)
                    outputFormat(option.toFormat)
                }

            // 3. 임시 파일 저장
            builder.toFile(tempFile)
            if (!tempFile.exists() || tempFile.length() == 0L) {
                throw IllegalStateException("Thumbnailator did not produce a valid temp file: ${tempFile.path}")
            }

            // 4. 워터마크 적용 (선택)
            if (!option.watermarkText.isNullOrBlank()) {
                applyTextWatermark(
                    sourceFile = tempFile,
                    targetFile = finalFile,
                    text = option.watermarkText!!,
                    position = option.watermarkPosition ?: WatermarkPosition.BOTTOM_RIGHT,
                    opacity = option.watermarkOpacity ?: 0.3f,
                )
                tempFile.delete()
            } else {
                if (!tempFile.renameTo(finalFile)) {
                    throw IllegalStateException("Failed to rename temp file to final file: ${finalFile.path}")
                }
            }

            // 5. 변환된 이미지 크기 반환
            val convertedSize = finalFile.length()
            logger.info("[Worker] Image processed: ${image.imageFileId}, size: $convertedSize")
            Result.success(convertedSize)
        } catch (e: Exception) {
            logger.error("[Worker] Failed to process image: ${image.imageFileId}", e)
            Result.failure(e)
        }

    private fun applyTextWatermark(
        sourceFile: File,
        targetFile: File,
        text: String,
        position: WatermarkPosition,
        opacity: Float,
    ) {
        val image: BufferedImage = ImageIO.read(sourceFile)
        val graphics: Graphics2D = image.createGraphics()

        val font =
            Font("Arial", Font.BOLD, 36).takeIf { it.canDisplayUpTo(text) == -1 }
                ?: Font("SansSerif", Font.BOLD, 36)
        graphics.font = font
        graphics.color = Color.WHITE
        graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)

        val fontMetrics = graphics.fontMetrics
        val textWidth = fontMetrics.stringWidth(text)
        val textHeight = fontMetrics.height

        val (x, y) = calculatePosition(position, image.width, image.height, textWidth, textHeight)
        graphics.drawString(text, x, y)
        graphics.dispose()

        // 확장자에 맞는 포맷으로 저장
        ImageIO.write(image, targetFile.extension, targetFile)
    }

    private fun calculatePosition(
        position: WatermarkPosition,
        imgWidth: Int,
        imgHeight: Int,
        textWidth: Int,
        textHeight: Int,
    ): Pair<Int, Int> =
        when (position) {
            WatermarkPosition.TOP_LEFT -> Pair(10, textHeight)
            WatermarkPosition.TOP_RIGHT -> Pair(imgWidth - textWidth - 10, textHeight)
            WatermarkPosition.BOTTOM_LEFT -> Pair(10, imgHeight - 10)
            WatermarkPosition.BOTTOM_RIGHT -> Pair(imgWidth - textWidth - 10, imgHeight - 10)
            WatermarkPosition.CENTER -> Pair((imgWidth - textWidth) / 2, (imgHeight + textHeight) / 2)
        }

    fun zipConvertedImages(jobId: Long): File {
        val targetDir = File("/tmp/$jobId/converted")
        val zipFile = File("/tmp/result/$jobId.zip")
        zipFile.parentFile.mkdirs()

        ZipOutputStream(zipFile.outputStream()).use { zipOut ->
            targetDir
                .walk()
                .filter { it.isFile }
                .forEach { file ->
                    zipOut.putNextEntry(ZipEntry(file.name))
                    file.inputStream().copyTo(zipOut)
                    zipOut.closeEntry()
                }
        }
        return zipFile
    }

    fun cleanTempFiles(jobId: Long) {
        val jobDir = File("/tmp/$jobId")
        if (jobDir.exists()) {
            jobDir.deleteRecursively()
            logger.info("[Worker] Temp files cleaned for jobId: $jobId")
        }
    }
}
