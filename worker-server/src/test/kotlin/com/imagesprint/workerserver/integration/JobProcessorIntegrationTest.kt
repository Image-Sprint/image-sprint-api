package com.imagesprint.workerserver.integration

import com.imagesprint.core.port.output.job.ReactiveConversionOptionRepository
import com.imagesprint.core.port.output.job.ReactiveImageRepository
import com.imagesprint.core.port.output.job.ReactiveJobRepository
import com.imagesprint.workerserver.processor.JobProcessor
import com.imagesprint.workerserver.support.MockUploaderConfig
import com.imagesprint.workerserver.support.TestEntityFactory
import com.imagesprint.workerserver.support.TestImageFileFactory
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest
@Import(MockUploaderConfig::class)
@ActiveProfiles("worker")
class JobProcessorIntegrationTest(
    @Autowired private val jobProcessor: JobProcessor,
    @Autowired private val imageRepository: ReactiveImageRepository,
    @Autowired private val optionRepository: ReactiveConversionOptionRepository,
    @Autowired private val jobRepository: ReactiveJobRepository,
) {
    @BeforeEach
    fun cleanup() =
        runBlocking {
            imageRepository.deleteAll()
            optionRepository.deleteAll()
            jobRepository.deleteAll()
        }

    @Test
    fun `통합 - JobProcessor가 전체 Job 처리 흐름을 성공적으로 수행한다`() =
        runTest {
            // given
            val savedJob = jobRepository.save(TestEntityFactory.job())
            val jobId = savedJob.jobId ?: error("jobId should not be null")

            val savedImage = imageRepository.save(TestEntityFactory.imageFile(jobId, fileName = "sample.jpg"))
            optionRepository.save(TestEntityFactory.conversionOption(jobId))

            TestImageFileFactory.create(jobId, savedImage.imageFileId!!, "sample.jpg")

            // when
            jobProcessor.process(jobId)

            // then
            val images = imageRepository.getAllForJob(jobId)
            images.forEach {
                assertThat(it.convertedSize).isGreaterThan(0L)
            }
        }
}
