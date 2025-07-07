package com.imagesprint.infrastructure.job.external

import com.imagesprint.core.port.output.job.JobQueuePort
import com.imagesprint.infrastructure.redis.RedisQueueRepository
import org.springframework.stereotype.Component

@Component
class JobQueueAdapter(
    private val redisQueueRepository: RedisQueueRepository,
) : JobQueuePort {
    override fun enqueueJob(
        jobId: Long,
        userId: Long,
        imageIds: List<Long>,
    ) {
        val message =
            mapOf(
                "jobId" to jobId,
                "userId" to userId,
                "imageIds" to imageIds,
            )
        redisQueueRepository.push("job:queue", message)
    }
}
