package com.imagesprint.workerserver.client

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.ReactiveListOperations
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import reactor.core.publisher.Mono
import kotlin.test.Test

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class CoroutineRedisQueueClientTest {
    @MockK
    lateinit var redisTemplate: ReactiveStringRedisTemplate

    @MockK
    lateinit var listOps: ReactiveListOperations<String, String>

    private lateinit var client: CoroutineRedisQueueClient

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { redisTemplate.opsForList() } returns listOps
        client = CoroutineRedisQueueClient(redisTemplate)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `단위 - consume에서 Redis에 값이 있으면 즉시 반환한다`() =
        runTest {
            // given
            every { listOps.leftPop("job:queue") } returns Mono.just("job-123")

            // when
            val result = client.consume("job:queue")

            // then
            assertThat(result).isEqualTo("job-123")
            verify(exactly = 1) { listOps.leftPop("job:queue") }
        }

    @Test
    fun `단위 - consume에서 처음엔 값이 없다가 나중에 값이 생기면 재시도 후 반환한다`() =
        runTest {
            // given
            every { listOps.leftPop("job:queue") } returnsMany
                listOf(
                    Mono.empty(),
                    Mono.empty(),
                    Mono.just("job-999"),
                )

            // when
            val result = async { client.consume("job:queue") }

            // then
            advanceTimeBy(2000) // 2초 딜레이 후 재시도 통과

            assertThat(result.await()).isEqualTo("job-999")
            verify(exactly = 3) { listOps.leftPop("job:queue") }
        }
}
