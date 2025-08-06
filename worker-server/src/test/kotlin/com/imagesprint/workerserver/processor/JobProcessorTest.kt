package com.imagesprint.workerserver.processor

import com.imagesprint.core.port.input.job.JobStatus
import com.imagesprint.core.port.output.job.ReactiveJobProgressRedisPort
import com.imagesprint.workerserver.client.HttpZipUploader
import com.imagesprint.workerserver.client.S3ClientAdapter
import com.imagesprint.workerserver.persistence.JobNotifier
import com.imagesprint.workerserver.persistence.JobReader
import com.imagesprint.workerserver.persistence.JobWriter
import com.imagesprint.workerserver.persistence.WebhookDispatcher
import com.imagesprint.workerserver.publisher.JobProgressRedisPublisher
import com.imagesprint.workerserver.support.TestEntityFactory
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Mono
import java.io.File
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class JobProcessorTest {
    @MockK
    lateinit var jobReader: JobReader

    @MockK
    lateinit var jobWriter: JobWriter

    @MockK
    lateinit var imageProcessor: ImageProcessor

    @MockK
    lateinit var s3ClientAdapter: S3ClientAdapter

    @MockK
    lateinit var uploader: HttpZipUploader

    @MockK
    lateinit var notifier: JobNotifier

    @MockK
    lateinit var webhookDispatcher: WebhookDispatcher

    @MockK
    lateinit var jobProgressRedisPort: ReactiveJobProgressRedisPort

    @MockK
    lateinit var jobProgressRedisPublisher: JobProgressRedisPublisher

    private lateinit var jobProcessor: JobProcessor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        jobProcessor =
            JobProcessor(
                jobReader,
                jobWriter,
                imageProcessor,
                s3ClientAdapter,
                uploader,
                notifier,
                webhookDispatcher,
                jobProgressRedisPort,
                jobProgressRedisPublisher,
            )
    }

    @Test
    fun `단위 - 정상적인 Job 처리 흐름을 수행한다`() =
        runTest {
            // given
            val jobId = 1L
            val userId = 42L
            val job = TestEntityFactory.job(jobId = jobId, userId = userId)
            val option = TestEntityFactory.conversionOption(jobId)
            val image = TestEntityFactory.imageFile(jobId).copy(imageFileId = 123L)
            val resultZip = File.createTempFile("result", ".zip")
            val presignedUrl = "https://example.com/presigned.zip"

            // Stub behaviors
            coEvery { jobReader.getJob(jobId) } returns job
            coEvery { notifier.notifyStarted(job) } just Runs
            coEvery { jobReader.getOption(jobId) } returns option
            coEvery { jobReader.getImages(jobId) } returns listOf(image)
            coEvery { jobWriter.markProcessing(jobId) } just Runs
            coEvery { jobProgressRedisPort.initProgress(jobId, 1) } just Runs
            coEvery { imageProcessor.processImage(jobId, image, option) } returns Result.success(1234L)
            coEvery { jobProgressRedisPort.incrementDone(jobId) } just Runs
            coEvery { jobProgressRedisPort.getProgress(jobId) } returns Pair(1, 1)
            every { jobProgressRedisPublisher.publish(jobId, 1, 1) } returns Mono.just(1L)
            coEvery { jobWriter.updateImageSize(image.imageFileId!!, 1234L) } just Runs
            every { imageProcessor.zipConvertedImages(jobId) } returns resultZip
            every { s3ClientAdapter.generatePresignedUploadUrl(userId, jobId) } returns presignedUrl
            every { uploader.upload(presignedUrl, resultZip) } just Runs
            every { s3ClientAdapter.generatePresignedDownloadUrl(userId, jobId) } returns presignedUrl
            coEvery {
                jobWriter.summarize(
                    jobId,
                    JobStatus.DONE,
                    doneCount = 1,
                    convertedSize = 1234L,
                    zipUrl = presignedUrl,
                    expiredAt = any(),
                )
            } just Runs
            coEvery { notifier.notifyFinished(job, true) } just Runs
            coEvery { webhookDispatcher.dispatch(userId, jobId, JobStatus.DONE) } just Runs
            every { imageProcessor.cleanTempFiles(jobId) } just Runs
            coEvery { jobProgressRedisPort.removeProgress(jobId) } just Runs

            // when
            jobProcessor.process(jobId)

            // then
            coVerify { jobReader.getJob(jobId) }
            coVerify { notifier.notifyStarted(job) }
            coVerify { jobReader.getOption(jobId) }
            coVerify { jobReader.getImages(jobId) }
            coVerify { jobWriter.markProcessing(jobId) }
            coVerify { jobProgressRedisPort.initProgress(jobId, 1) }
            coVerify { imageProcessor.processImage(jobId, image, option) }
            coVerify { jobProgressRedisPort.incrementDone(jobId) }
            coVerify { jobProgressRedisPort.getProgress(jobId) }
            coVerify { jobProgressRedisPort.removeProgress(jobId) }
            coVerify { jobProgressRedisPublisher.publish(jobId, 1, 1) }
            coVerify { jobWriter.updateImageSize(image.imageFileId!!, 1234L) }
            coVerify { imageProcessor.zipConvertedImages(jobId) }
            coVerify { s3ClientAdapter.generatePresignedUploadUrl(userId, jobId) }
            coVerify { uploader.upload(presignedUrl, resultZip) }
            coVerify { s3ClientAdapter.generatePresignedDownloadUrl(userId, jobId) }
            coVerify {
                jobWriter.summarize(
                    jobId,
                    JobStatus.DONE,
                    1,
                    1234L,
                    presignedUrl,
                    any(),
                )
            }
            coVerify { notifier.notifyFinished(job, true) }
            coVerify { webhookDispatcher.dispatch(userId, jobId, JobStatus.DONE) }
            coVerify { imageProcessor.cleanTempFiles(jobId) }
        }

    @Test
    fun `단위 - 존재하지 않는 Job은 처리하지 않는다`() =
        runTest {
            // given
            val jobId = 99L
            coEvery { jobReader.getJob(jobId) } returns null

            // when
            jobProcessor.process(jobId)

            // then
            coVerify(exactly = 1) { jobReader.getJob(jobId) }
            confirmVerified(
                jobReader,
                notifier,
                jobWriter,
                imageProcessor,
                uploader,
                s3ClientAdapter,
                webhookDispatcher,
            )
        }
}
