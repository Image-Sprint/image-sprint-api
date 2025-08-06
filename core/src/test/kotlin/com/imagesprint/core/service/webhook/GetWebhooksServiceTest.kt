package com.imagesprint.core.service.webhook

import com.imagesprint.core.domain.webhook.WebhookType
import com.imagesprint.core.port.output.webhook.WebhookRepository
import com.imagesprint.core.support.factory.WebhookTestFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class GetWebhooksServiceTest {
    private lateinit var webhookRepository: WebhookRepository
    private lateinit var getWebhooksService: GetWebhooksService

    @BeforeEach
    fun setUp() {
        webhookRepository = mockk()
        getWebhooksService = GetWebhooksService(webhookRepository)
    }

    @Test
    fun `단위 - 유저 ID로 등록된 모든 웹훅을 조회한다`() {
        // given
        val userId = 1L
        val webhooks =
            listOf(
                WebhookTestFactory.create(webhookId = 1L, type = WebhookType.DISCORD),
                WebhookTestFactory.create(webhookId = 2L, type = WebhookType.SLACK),
            )
        every { webhookRepository.getAllOf(userId) } returns webhooks

        // when
        val result = getWebhooksService.get(userId)

        // then
        assertThat(result).hasSize(2)
        assertThat(result).isEqualTo(webhooks)
        verify(exactly = 1) { webhookRepository.getAllOf(userId) }
    }
}
