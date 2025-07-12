package com.imagesprint.core.port.output.job

interface ReactiveJobProgressRedisPort {
    suspend fun initProgress(
        jobId: Long,
        total: Int,
    )

    suspend fun incrementDone(jobId: Long)

    suspend fun getProgress(jobId: Long): Pair<Int, Int>

    suspend fun removeProgress(jobId: Long)
}
