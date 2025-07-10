package com.imagesprint.core.service.job

import com.imagesprint.core.port.output.job.JobRepository
import com.imagesprint.core.support.factory.JobTestFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class GetMyJobsServiceTest {
    private lateinit var jobRepository: JobRepository
    private lateinit var getMyJobsService: GetMyJobsService

    @BeforeEach
    fun setUp() {
        jobRepository = mockk()
        getMyJobsService = GetMyJobsService(jobRepository)
    }

    @Test
    fun `단위 - 정상적으로 사용자의 Job 목록을 조회한다`() {
        // given
        val userId = 1L
        val jobList =
            listOf(
                JobTestFactory.create(jobId = 101L),
                JobTestFactory.create(jobId = 102L),
            )
        every { jobRepository.getMyJobs(userId) } returns jobList

        // when
        val result = getMyJobsService.getMyJobs(userId)

        // then
        assertThat(result).hasSize(2)
        assertThat(result[0].jobId).isEqualTo(101L)
        assertThat(result[1].jobId).isEqualTo(102L)
        verify(exactly = 1) { jobRepository.getMyJobs(userId) }
    }

    @Test
    fun `단위 - 사용자의 Job이 하나도 없으면 빈 리스트를 반환한다`() {
        // given
        val userId = 2L
        every { jobRepository.getMyJobs(userId) } returns emptyList()

        // when
        val result = getMyJobsService.getMyJobs(userId)

        // then
        assertThat(result).isEmpty()
        verify(exactly = 1) { jobRepository.getMyJobs(userId) }
    }

    @Test
    fun `단위 - Job의 ID가 null인 경우는 변환에서 예외가 발생해야 한다`() {
        // given
        val userId = 3L
        val job = JobTestFactory.create(jobId = null)
        every { jobRepository.getMyJobs(userId) } returns listOf(job)

        // when & then
        assertThatThrownBy {
            getMyJobsService.getMyJobs(userId)
        }.isInstanceOf(NullPointerException::class.java)
    }
}
