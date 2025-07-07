package com.imagesprint.infrastructure.redis

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisQueueRepository(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) {
    fun push(
        queueKey: String,
        payload: Any,
    ) {
        val json = objectMapper.writeValueAsString(payload)
        redisTemplate.opsForList().leftPush(queueKey, json)
    }

    fun pop(queueKey: String): String? = redisTemplate.opsForList().rightPop(queueKey)

    fun peek(queueKey: String): String? = redisTemplate.opsForList().index(queueKey, -1)
}
