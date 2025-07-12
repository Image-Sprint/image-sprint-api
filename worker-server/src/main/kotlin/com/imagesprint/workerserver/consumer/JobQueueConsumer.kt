package com.imagesprint.workerserver.consumer

import com.imagesprint.infrastructure.redis.queue.CoroutineRedisQueueClient
import com.imagesprint.workerserver.processor.JobProcessor
import com.imagesprint.workerserver.publisher.JobProgressRedisPublisher
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class JobQueueConsumer(
    private val queueClient: CoroutineRedisQueueClient,
    private val jobProcessor: JobProcessor,
    private val publisher: JobProgressRedisPublisher,
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

    /**
     * 모든 작업(Job)이 없는 상태에서도 SSE 연결을 유지하기 위해
     * 주기적으로 dummy 'progress' 이벤트를 Redis Pub/Sub으로 발행한다.
     *
     * 발행된 이벤트는 jobId = -1, done = -1, total = -1 값을 가지며,
     * 클라이언트는 해당 값을 무시하도록 처리하여 실제 작업 진행과 구분한다.
     *
     * ⚠️ 이 ping은 서버 전역에서 1분마다 주기적으로 발생하며,
     *     - 클라이언트 측 EventSource 연결이 idle로 간주되어 끊어지는 것을 방지하고,
     *     - 브라우저나 프록시에서 타임아웃 되지 않도록 keep-alive 용도로 사용된다.
     *
     * 이 메서드는 애플리케이션 시작 시 1회 호출되어 백그라운드에서 루프로 계속 동작해야 하며,
     * cancellation 없이 JVM 종료 전까지 유지되어야 한다.
     */
    fun startKeepAlivePings() {
        scope.launch {
            while (true) {
                delay(60_000)
                try {
                    publisher.publish(-1, -1, -1).awaitSingle()
                    logger.info("[JobQueueConsumer] global keep-alive ping sent")
                } catch (e: Exception) {
                    logger.warn("[JobQueueConsumer] ping publish 실패: ${e.message}")
                }
            }
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
