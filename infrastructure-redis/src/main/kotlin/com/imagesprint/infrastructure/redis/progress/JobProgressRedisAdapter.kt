package com.imagesprint.infrastructure.redis.progress

import com.imagesprint.core.port.output.job.ReactiveJobProgressRedisPort
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component

@Component
@Profile("worker")
class JobProgressRedisAdapter(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
) : ReactiveJobProgressRedisPort {
    override suspend fun initProgress(
        jobId: Long,
        total: Int,
    ) {
        val ops = redisTemplate.opsForHash<String, String>()
        ops.put(jobId.toKey(), "done", "0").subscribe()
        ops.put(jobId.toKey(), "total", total.toString()).subscribe()
    }

    override suspend fun incrementDone(jobId: Long) {
        val ops = redisTemplate.opsForHash<String, String>()
        ops.increment(jobId.toKey(), "done", 1).subscribe()
    }

    override suspend fun getProgress(jobId: Long): Pair<Int, Int> {
        val ops = redisTemplate.opsForHash<String, String>()
        val done = ops.get(jobId.toKey(), "done").awaitFirstOrNull()?.toIntOrNull() ?: 0
        val total = ops.get(jobId.toKey(), "total").awaitFirstOrNull()?.toIntOrNull() ?: 0
        return done to total
    }

    override suspend fun removeProgress(jobId: Long) {
        redisTemplate.delete(jobId.toKey()).subscribe()
    }

    private fun Long.toKey() = "job:progress:$this"
}
