package com.imagesprint.workerserver.processor

import com.imagesprint.core.port.input.job.JobStatus
import com.imagesprint.workerserver.client.*
import com.imagesprint.workerserver.support.TestEntityFactory
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
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
            val image = TestEntityFactory.imageFile(jobId)
            val imageFileId = 123L
            val resultZip = File.createTempFile("result", ".zip")
            val presignedUrl = "https://example.com/presigned.zip"

            val imageWithId = image.copy(imageFileId = imageFileId)

            coEvery { jobReader.getJob(jobId) } returns job
            coEvery { notifier.notifyStarted(job) } just Runs
            coEvery { jobReader.getOption(jobId) } returns option
            coEvery { jobReader.getImages(jobId) } returns listOf(imageWithId)
            coEvery { jobWriter.markProcessing(jobId) } just Runs
            coEvery { imageProcessor.processImage(jobId, imageWithId, option) } returns Result.success(1234L)
            coEvery { jobWriter.incrementProgress(jobId, 1234L) } just Runs
            coEvery { jobWriter.updateImageSize(imageFileId, 1234L) } just Runs
            every { imageProcessor.zipConvertedImages(jobId) } returns resultZip
            every { s3ClientAdapter.generatePresignedUrl(userId, jobId) } returns presignedUrl
            every { uploader.upload(presignedUrl, resultZip) } just Runs
            coEvery {
                jobWriter.summarize(
                    jobId,
                    JobStatus.DONE,
                    doneCount = 1,
                    convertedSize = 1234L,
                    zipUrl = presignedUrl,
                )
            } just Runs
            coEvery { notifier.notifyFinished(job, true) } just Runs
            coEvery { webhookDispatcher.dispatch(userId, jobId, JobStatus.DONE) } just Runs
            every { imageProcessor.cleanTempFiles(jobId) } just Runs

            // when
            jobProcessor.process(jobId)

            // then
            coVerifySequence {
                jobReader.getJob(jobId)
                notifier.notifyStarted(job)
                jobReader.getOption(jobId)
                jobReader.getImages(jobId)
                jobWriter.markProcessing(jobId)
                imageProcessor.processImage(jobId, imageWithId, option)
                jobWriter.incrementProgress(jobId, 1234L)
                jobWriter.updateImageSize(imageFileId, 1234L)
                imageProcessor.zipConvertedImages(jobId)
                s3ClientAdapter.generatePresignedUrl(userId, jobId)
                uploader.upload(presignedUrl, resultZip)
                jobWriter.summarize(jobId, JobStatus.DONE, 1, 1234L, presignedUrl)
                notifier.notifyFinished(job, true)
                webhookDispatcher.dispatch(userId, jobId, JobStatus.DONE)
                imageProcessor.cleanTempFiles(jobId)
            }
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
