package com.imagesprint.core.service.job

import com.imagesprint.core.port.input.job.GetJobPageQuery
import com.imagesprint.core.port.input.job.GetMyJobsUseCase
import com.imagesprint.core.port.input.job.JobPage
import com.imagesprint.core.port.output.job.JobRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Profile("api")
@Service
class GetMyJobsService(
    private val jobRepository: JobRepository,
) : GetMyJobsUseCase {
    @Transactional(readOnly = true)
    override fun getMyJobs(query: GetJobPageQuery): JobPage =
        jobRepository.getMyJobsByCursor(
            userId = query.userId,
            cursor = query.cursor,
            pageSize = query.pageSize,
        )
}
