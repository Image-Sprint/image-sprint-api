package com.imagesprint.workerserver.consumer

import com.imagesprint.workerserver.client.CoroutineRedisQueueClient
import com.imagesprint.workerserver.processor.JobProcessor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class JobQueueConsumer(
    private val queueClient: CoroutineRedisQueueClient,
    private val jobProcessor: JobProcessor,
) {
    private val logger = LoggerFactory.getLogger(JobQueueConsumer::class.java)

    suspend fun consumeLoop() {
        logger.info("[JobQueueConsumer] started consumeLoop")
        while (true) {
            try {
                val jobIdString = queueClient.consume("job:queue") ?: continue
                logger.debug("[JobQueueConsumer] Consumed jobId string: {}", jobIdString)

                val jobId = jobIdString.toLongOrNull()
                if (jobId == null) {
                    logger.warn("[JobQueueConsumer] Invalid jobId string format: {}", jobIdString)
                    continue
                }

                logger.info("[JobQueueConsumer] Start processing jobId: {}", jobId)
                jobProcessor.process(jobId)
                logger.info("[JobQueueConsumer] Finished processing jobId: {}", jobId)
            } catch (e: Exception) {
                logger.error("[JobQueueConsumer] Exception while processing job from Redis", e)
            }
        }
    }
}
