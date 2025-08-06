package com.imagesprint.workerserver.consumer

import com.imagesprint.infrastructure.redis.queue.CoroutineRedisQueueClient
import com.imagesprint.workerserver.processor.JobProcessor
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class JobQueueConsumer(
    private val queueClient: CoroutineRedisQueueClient,
    private val jobProcessor: JobProcessor,
) {
    private val logger = LoggerFactory.getLogger(JobQueueConsumer::class.java)

    private val maxParallelism = 50 // 최대 동시 처리 개수
    private val semaphore = Semaphore(maxParallelism)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun startConsuming() {
        logger.info("[JobQueueConsumer] 병렬 제한($maxParallelism)으로 consumeLoop 시작")
        scope.launch {
            consumeLoop()
        }
    }

    private suspend fun consumeLoop() {
        while (true) {
            try {
                val jobIdString = queueClient.consume("job:queue") ?: continue
                val jobId = jobIdString.trim().removeSurrounding("\"").toLongOrNull() ?: continue

                scope.launch {
                    logger.info("[JobQueueConsumer] Start jobId: $jobId")

                    semaphore.withPermit {
                        try {
                            jobProcessor.process(jobId)
                            logger.info("[JobQueueConsumer] Finished jobId: $jobId")
                        } catch (e: Exception) {
                            logger.error("[JobQueueConsumer] Job processing failed", e)
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("[JobQueueConsumer] consumeLoop 에러 발생", e)
                delay(1000L)
            }
        }
    }
}
