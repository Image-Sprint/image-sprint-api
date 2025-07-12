package com.imagesprint.core.service.job

import com.imagesprint.core.port.input.job.GetJobPageQuery
import com.imagesprint.core.port.input.job.JobPage
import com.imagesprint.core.port.output.job.JobRepository
import com.imagesprint.core.support.factory.JobTestFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
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
        val jobList =
            JobPage(
                jobs =
                    listOf(
                        JobTestFactory.create(jobId = 101L),
                        JobTestFactory.create(jobId = 102L),
                    ),
                hasNext = false,
                nextCursor = null,
            )
        val query =
            GetJobPageQuery(
                userId = 1L,
                cursor = null,
                pageSize = 3,
            )
        every { jobRepository.getMyJobsByCursor(query.userId, query.cursor, query.pageSize) } returns jobList

        // when
        val result = getMyJobsService.getMyJobs(query)

        // then
        assertThat(result.jobs).hasSize(2)
        assertThat(result.jobs[0].jobId).isEqualTo(101L)
        assertThat(result.jobs[1].jobId).isEqualTo(102L)
        verify(exactly = 1) { jobRepository.getMyJobsByCursor(query.userId, query.cursor, query.pageSize) }
    }

    @Test
    fun `단위 - 사용자의 Job이 하나도 없으면 빈 리스트를 반환한다`() {
        // given
        val jobList =
            JobPage(
                jobs =
                    listOf(),
                hasNext = false,
                nextCursor = null,
            )
        val query =
            GetJobPageQuery(
                userId = 1L,
                cursor = null,
                pageSize = 3,
            )
        every { jobRepository.getMyJobsByCursor(query.userId, query.cursor, query.pageSize) } returns jobList

        // when
        val result = getMyJobsService.getMyJobs(query)

        // then
        assertThat(result.jobs).isEmpty()
        verify(exactly = 1) { jobRepository.getMyJobsByCursor(query.userId, query.cursor, query.pageSize) }
    }
}
