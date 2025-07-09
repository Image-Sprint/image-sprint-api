package com.imagesprint.workerserver.client

import com.imagesprint.core.domain.job.ConversionOption
import com.imagesprint.core.domain.job.ImageFile
import com.imagesprint.core.domain.job.Job
import com.imagesprint.core.port.output.job.ReactiveConversionOptionRepository
import com.imagesprint.core.port.output.job.ReactiveImageRepository
import com.imagesprint.core.port.output.job.ReactiveJobRepository
import org.springframework.stereotype.Component

@Component
class JobReader(
    private val jobRepository: ReactiveJobRepository,
    private val optionRepository: ReactiveConversionOptionRepository,
    private val imageRepository: ReactiveImageRepository,
) {
    suspend fun getJob(jobId: Long): Job? = jobRepository.getJob(jobId)

    suspend fun getOption(jobId: Long): ConversionOption = optionRepository.getForJob(jobId)

    suspend fun getImages(jobId: Long): List<ImageFile> = imageRepository.getAllForJob(jobId)
}
