package com.imagesprint.apiserver.consumer

import com.imagesprint.core.port.input.job.SubscribeJobProgressUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

@Component
class JobProgressStreamConsumer(
    private val jobProgressUseCase: SubscribeJobProgressUseCase,
) {
    private val logger = LoggerFactory.getLogger(JobProgressStreamConsumer::class.java)

    fun subscribe(emitter: SseEmitter) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        val job =
            scope.launch {
                // progress 이벤트 스트리밍
                jobProgressUseCase
                    .subscribeAll()
                    .onEach { progress ->
                        sendEventSafely(emitter, progress)
                    }.catch { e ->
                        logger.error("SSE 스트림 예외 발생", e)
                        completeSafely(emitter, error = e)
                    }.onCompletion { cause ->
                        if (cause == null) {
                            logger.info("SSE emitter 정상 종료")
                            completeSafely(emitter)
                        }
                    }.collect {}
            }

        emitter.onCompletion {
            logger.info("클라이언트에서 SSE 연결 종료")
            job.cancel()
        }

        emitter.onTimeout {
            logger.info("SSE 타임아웃 발생으로 종료")
            job.cancel()
        }
    }

    private fun sendEventSafely(
        emitter: SseEmitter,
        progress: Any,
    ) {
        try {
            emitter.send(SseEmitter.event().name("progress").data(progress))
        } catch (e: IOException) {
            logger.warn("SSE IOException (Broken pipe?): {}", e.message)
            emitter.complete()
            throw CancellationException("Broken pipe", e)
        } catch (e: IllegalStateException) {
            logger.warn("SSE 연결 상태 이상 (IllegalState): {}", e.message)
            emitter.complete()
            throw CancellationException("IllegalState", e)
        } catch (e: Exception) {
            logger.error("SSE send 중 예외 발생", e)
            emitter.complete()
            throw CancellationException("Unexpected SSE error", e)
        }
    }

    private fun completeSafely(
        emitter: SseEmitter,
        error: Throwable? = null,
    ) {
        runCatching {
            if (error != null) {
                emitter.completeWithError(error)
            } else {
                emitter.complete()
            }
        }.onFailure {
            logger.warn("emitter 완료 중 오류 발생: {}", it.message)
        }
    }
}
