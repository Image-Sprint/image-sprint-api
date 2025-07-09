package com.imagesprint.core.port.output.job

import com.imagesprint.core.domain.job.ConversionOption

interface ReactiveConversionOptionRepository {
    suspend fun save(option: ConversionOption): ConversionOption

    suspend fun deleteAll()

    suspend fun getForJob(jobId: Long): ConversionOption
}
