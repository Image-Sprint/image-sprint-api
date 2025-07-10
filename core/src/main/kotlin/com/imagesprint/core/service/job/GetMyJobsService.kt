package com.imagesprint.core.service.job

import com.imagesprint.core.port.input.job.GetMyJobsUseCase
import com.imagesprint.core.port.input.job.JobResult
import com.imagesprint.core.port.output.job.JobRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("api")
@Service
class GetMyJobsService(
    private val jobRepository: JobRepository,
) : GetMyJobsUseCase {
    override fun getMyJobs(userId: Long): List<JobResult> =
        jobRepository.getMyJobs(userId).map { job ->
            JobResult(
                jobId = job.jobId!!,
                status = job.status.name,
                imageCount = job.imageCount,
                originalSize = job.originalSize,
                createdAt = job.createdAt,
                zirUrl = job.zipUrl,
            )
        }
}
