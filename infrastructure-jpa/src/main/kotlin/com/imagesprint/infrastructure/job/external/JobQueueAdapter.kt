package com.imagesprint.infrastructure.job.external

import com.imagesprint.core.port.output.job.JobQueuePort
import com.imagesprint.infrastructure.redis.RedisQueueRepository
import org.springframework.stereotype.Component

@Component
class JobQueueAdapter(
    private val redisQueueRepository: RedisQueueRepository,
) : JobQueuePort {
    override fun enqueueJob(jobId: Long) {
        redisQueueRepository.push("job:queue", jobId.toString())
    }
}
