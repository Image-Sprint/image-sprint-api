package com.imagesprint.core.service.webhook

import com.imagesprint.core.exception.CustomException
import com.imagesprint.core.exception.ErrorCode
import com.imagesprint.core.port.output.webhook.WebhookRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class DeleteWebhookServiceTest {
    private lateinit var webhookRepository: WebhookRepository
    private lateinit var deleteWebhookService: DeleteWebhookService

    @BeforeEach
    fun setUp() {
        webhookRepository = mockk()
        deleteWebhookService = DeleteWebhookService(webhookRepository)
    }

    @Test
    fun `단위 - 삭제 요청 시, 유저 소유의 웹훅이 정상적으로 제거된다`() {
        // given
        val userId = 1L
        val webhookId = 100L
        every { webhookRepository.removeOwnedBy(userId, webhookId) } returns true

        // when
        deleteWebhookService.delete(userId, webhookId)

        // then
        verify(exactly = 1) { webhookRepository.removeOwnedBy(1L, 100L) }
    }

    @Test
    fun `단위 - 존재하지 않는 웹훅을 삭제하려 하면 예외가 발생한다`() {
        // given
        val userId = 1L
        val webhookId = 999L
        every { webhookRepository.removeOwnedBy(userId, webhookId) } returns false

        // when & then
        assertThatThrownBy {
            deleteWebhookService.delete(userId, webhookId)
        }.isInstanceOf(CustomException::class.java)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WEBHOOK_NOT_FOUND)

        verify(exactly = 1) { webhookRepository.removeOwnedBy(userId, webhookId) }
    }
}
