package com.imagesprint.infrastructure.redis.queue

import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component

@Component
class CoroutineRedisQueueClient(
    private val redisTemplate: ReactiveStringRedisTemplate,
) {
    private val logger = LoggerFactory.getLogger(CoroutineRedisQueueClient::class.java)

    companion object {
        private const val POLLING_INTERVAL_MS = 500L
        private const val INITIAL_DELAY_MS = 500L
    }

    suspend fun consume(queueKey: String): String? {
        logger.info("[Redis] Start consuming queue: {}", queueKey)

        delay(INITIAL_DELAY_MS)

        while (true) {
            try {
                val result =
                    redisTemplate
                        .opsForList()
                        .leftPop(queueKey)
                        .awaitSingleOrNull()

                if (result != null) {
                    logger.debug("[Redis] Consumed jobId from {}: {}", queueKey, result)
                    return result
                } else {
                    logger.debug("[Redis] Queue '{}' empty, waiting...", queueKey)
                    delay(POLLING_INTERVAL_MS)
                }
            } catch (e: Exception) {
                logger.error("[Redis] Error while consuming from {}", queueKey, e)
                delay(POLLING_INTERVAL_MS)
            }
        }
    }
}
