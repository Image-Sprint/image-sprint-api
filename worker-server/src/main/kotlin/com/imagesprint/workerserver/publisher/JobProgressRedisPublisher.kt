package com.imagesprint.workerserver.publisher

import com.fasterxml.jackson.databind.ObjectMapper
import com.imagesprint.core.port.input.job.JobProgressResult
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JobProgressRedisPublisher(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val objectMapper: ObjectMapper,
) {
    private val topic = ChannelTopic("job:progress:stream")
    private val log = LoggerFactory.getLogger(JobProgressRedisPublisher::class.java)

    fun publish(
        jobId: Long,
        done: Int,
        total: Int,
    ): Mono<Long> {
        val payload =
            objectMapper.writeValueAsString(
                JobProgressResult(jobId, done, total),
            )
//        log.warn("🔥 RedisPublisher.publish() called!")
//        log.info("📤 [Redis] Publishing job progress: jobId=$jobId, $done/$total -> $payload")

        return redisTemplate
            .convertAndSend(topic.topic, payload)
            .doOnSuccess { count ->
//                log.info("✅ [Redis] Message sent to $count subscriber(s) for jobId=$jobId")
            }.doOnError { e ->
                log.error("❌ [Redis] Failed to publish progress for jobId=$jobId", e)
            }
    }
}
