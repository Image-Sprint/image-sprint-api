package com.imagesprint.workerserver.processor

import com.imagesprint.core.port.input.job.JobStatus
import com.imagesprint.workerserver.client.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("worker")
@Component
class JobProcessor(
    private val jobReader: JobReader,
    private val jobWriter: JobWriter,
    private val imageProcessor: ImageProcessor,
    private val s3ClientAdapter: S3ClientAdapter,
    private val uploader: ZipUploader,
    private val notifier: JobNotifier,
    private val webhookDispatcher: WebhookDispatcher,
) {
    private val logger = LoggerFactory.getLogger(JobProcessor::class.java)

    suspend fun process(jobId: Long) =
        coroutineScope {
            logger.info("[Worker] JobProcessor invoked with jobId = $jobId")
            val job = jobReader.getJob(jobId)
            if (job == null) {
                logger.warn("[Worker] Job not found: $jobId")
                return@coroutineScope
            }

            notifier.notifyStarted(job)
            val option = jobReader.getOption(jobId)
            val images = jobReader.getImages(jobId)

            jobWriter.markProcessing(jobId)

            val results =
                images
                    .map { image ->
                        async(Dispatchers.IO) {
                            val result = imageProcessor.processImage(jobId, image, option)
                            if (result.isSuccess) {
                                val convertedSize = result.getOrNull() ?: 0L
                                jobWriter.incrementProgress(jobId, convertedSize)
                                jobWriter.updateImageSize(image.imageFileId!!, convertedSize)
                            }
                            result
                        }
                    }.awaitAll()

            val successCount = results.count { it.isSuccess }
            val failedCount = results.size - successCount
            val totalConvertedSize = results.sumOf { it.getOrNull() ?: 0L }

            var zipUrl: String? = null
            try {
                val zipFile = imageProcessor.zipConvertedImages(jobId)
                zipUrl = s3ClientAdapter.generatePresignedUrl(job.userId, jobId)
                uploader.upload(zipUrl, zipFile)
            } catch (e: Exception) {
                logger.error("[Worker] ZIP upload failed", e)
            }

            jobWriter.summarize(
                jobId = jobId,
                status = if (failedCount == 0) JobStatus.DONE else JobStatus.FAILED,
                doneCount = successCount,
                convertedSize = totalConvertedSize,
                zipUrl = zipUrl ?: "",
            )

            notifier.notifyFinished(job, failedCount == 0)
            webhookDispatcher.dispatch(job.userId, jobId, if (failedCount == 0) JobStatus.DONE else JobStatus.FAILED)
            imageProcessor.cleanTempFiles(jobId)

            logger.info("[Worker] Job processed: $jobId, success=$successCount, failed=$failedCount")
        }
}
