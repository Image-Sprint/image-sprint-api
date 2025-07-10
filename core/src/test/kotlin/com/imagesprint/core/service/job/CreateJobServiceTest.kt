package com.imagesprint.core.service.job

import com.imagesprint.core.exception.CustomException
import com.imagesprint.core.exception.ErrorCode
import com.imagesprint.core.port.output.job.*
import com.imagesprint.core.support.factory.ConversionOptionTestFactory
import com.imagesprint.core.support.factory.CreateJobCommandTestFactory
import com.imagesprint.core.support.factory.ImageFileTestFactory
import com.imagesprint.core.support.factory.JobTestFactory
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class CreateJobServiceTest {
    private lateinit var jobRepository: JobRepository
    private lateinit var imageRepository: ImageRepository
    private lateinit var conversionOptionRepository: ConversionOptionRepository
    private lateinit var fileStoragePort: FileStoragePort
    private lateinit var jobQueuePort: JobQueuePort
    private lateinit var createJobService: CreateJobService

    @BeforeEach
    fun setUp() {
        jobRepository = mockk()
        imageRepository = mockk()
        conversionOptionRepository = mockk()
        fileStoragePort = mockk()
        jobQueuePort = mockk()
        createJobService =
            CreateJobService(
                jobRepository,
                imageRepository,
                conversionOptionRepository,
                fileStoragePort,
                jobQueuePort,
            )
    }

    @Test
    fun `정상적으로 이미지 변환 Job을 생성하고 큐에 등록한다`() {
        // given
        val command = CreateJobCommandTestFactory.valid()
        val fakeImages = listOf(ImageFileTestFactory.create(), ImageFileTestFactory.create())
        val savedJob = JobTestFactory.create(jobId = 100L)
        val savedOption = ConversionOptionTestFactory.create(jobId = savedJob.jobId!!)

        every { imageRepository.saveImages(any()) } returns fakeImages
        every { fileStoragePort.saveOriginalFiles(any(), any(), any()) } just Runs
        every { jobRepository.saveJob(any()) } returns savedJob
        every { conversionOptionRepository.saveOption(any()) } returns savedOption
        every { imageRepository.updateJobIdAndStatus(any(), any()) } just Runs
        every { jobQueuePort.enqueueJob(any()) } just Runs

        // when
        val result = createJobService.execute(command)

        // then
        assertThat(result.jobId).isEqualTo(100L)
        assertThat(result.imageCount).isEqualTo(2)
        verify(exactly = 1) { imageRepository.saveImages(any()) }
        verify(exactly = 1) { fileStoragePort.saveOriginalFiles(any(), any(), any()) }
        verify(exactly = 1) { jobRepository.saveJob(any()) }
        verify(exactly = 1) { conversionOptionRepository.saveOption(any()) }
        verify(exactly = 1) { imageRepository.updateJobIdAndStatus(any(), any()) }
        verify(exactly = 1) { jobQueuePort.enqueueJob(any()) }
    }

    @Test
    fun `quality 값이 유효하지 않으면 CustomException이 발생한다`() {
        // given
        val invalidCommand = CreateJobCommandTestFactory.withQuality(150)

        // when & then
        assertThatThrownBy {
            createJobService.execute(invalidCommand)
        }.isInstanceOf(CustomException::class.java)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_QUALITY)
    }

    @Test
    fun `파일 저장 중 예외가 발생하면 트랜잭션 롤백과 함께 CustomException이 발생한다`() {
        // given
        val command = CreateJobCommandTestFactory.valid()
        val fakeImages = listOf(ImageFileTestFactory.create(), ImageFileTestFactory.create())
        val savedJob = JobTestFactory.create(jobId = 200L)
        val savedOption = ConversionOptionTestFactory.create(jobId = savedJob.jobId!!)

        every { imageRepository.saveImages(any()) } returns fakeImages
        every { fileStoragePort.saveOriginalFiles(any(), any(), any()) } throws RuntimeException("Disk full")
        every { jobRepository.saveJob(any()) } returns savedJob
        every { conversionOptionRepository.saveOption(any()) } returns savedOption
        every { imageRepository.updateJobIdAndStatus(any(), any()) } just Runs

        // when & then
        assertThatThrownBy {
            createJobService.execute(command)
        }.isInstanceOf(CustomException::class.java)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FILE_STORAGE_FAILED)

        verify(exactly = 1) { imageRepository.saveImages(any()) }
        verify(exactly = 1) { fileStoragePort.saveOriginalFiles(any(), any(), any()) }
        verify(exactly = 1) { conversionOptionRepository.saveOption(any()) }
        verify(exactly = 1) { imageRepository.updateJobIdAndStatus(any(), any()) }
        verify(exactly = 0) { jobQueuePort.enqueueJob(any()) }
    }

    @Test
    fun `큐 등록 중 예외가 발생하면 트랜잭션 롤백과 함께 CustomException이 발생한다`() {
        // given
        val command = CreateJobCommandTestFactory.valid()
        val fakeImages = listOf(ImageFileTestFactory.create(), ImageFileTestFactory.create())
        val savedJob = JobTestFactory.create(jobId = 300L)
        val savedOption = ConversionOptionTestFactory.create(jobId = savedJob.jobId!!)

        every { imageRepository.saveImages(any()) } returns fakeImages
        every { fileStoragePort.saveOriginalFiles(any(), any(), any()) } just Runs
        every { jobRepository.saveJob(any()) } returns savedJob
        every { conversionOptionRepository.saveOption(any()) } returns savedOption
        every { imageRepository.updateJobIdAndStatus(any(), any()) } just Runs
        every { jobQueuePort.enqueueJob(any()) } throws RuntimeException("Redis down")

        // when & then
        assertThatThrownBy {
            createJobService.execute(command)
        }.isInstanceOf(CustomException::class.java)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.QUEUE_ENQUEUE_FAILED)

        verify(exactly = 1) { jobQueuePort.enqueueJob(any()) }
    }
}
