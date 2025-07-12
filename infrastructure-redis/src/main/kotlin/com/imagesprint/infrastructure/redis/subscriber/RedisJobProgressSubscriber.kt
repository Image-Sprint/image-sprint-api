package com.imagesprint.infrastructure.redis.subscriber

import com.fasterxml.jackson.databind.ObjectMapper
import com.imagesprint.core.port.input.job.JobProgressResult
import com.imagesprint.core.port.input.job.SubscribeJobProgressUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.stereotype.Component
import reactor.core.publisher.Sinks

@Component
class RedisJobProgressSubscriber(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : SubscribeJobProgressUseCase {
    private val log = LoggerFactory.getLogger(this::class.java)

    // 여러 클라이언트가 동시에 구독 가능하게 하는 멀티캐스트 Sink
    private val sink = Sinks.many().multicast().onBackpressureBuffer<JobProgressResult>()

    init {
        val topic = PatternTopic("job:progress:stream")
        val container = ReactiveRedisMessageListenerContainer(redisTemplate.connectionFactory)

        container
            .receive(topic)
            .mapNotNull {
                val json = it.message
                runCatching {
                    objectMapper.readValue(json, JobProgressResult::class.java)
                }.onFailure { e ->
                    log.error("JSON 역직렬화 실패", e)
                }.getOrNull()
            }.subscribe { progress ->
                sink.tryEmitNext(progress!!)
            }
    }

    override fun subscribeAll(): Flow<JobProgressResult> = sink.asFlux().asFlow()
}
