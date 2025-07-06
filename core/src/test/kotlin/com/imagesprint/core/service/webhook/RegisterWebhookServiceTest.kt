package com.imagesprint.core.service.webhook

import com.imagesprint.core.domain.webhook.WebhookType
import com.imagesprint.core.port.input.webhook.RegisterWebhookCommand
import com.imagesprint.core.port.output.webhook.WebhookRepository
import com.imagesprint.core.support.factory.WebhookTestFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class RegisterWebhookServiceTest {
    private lateinit var webhookRepository: WebhookRepository
    private lateinit var registerWebhookService: RegisterWebhookService

    @BeforeEach
    fun setUp() {
        webhookRepository = mockk()
        registerWebhookService = RegisterWebhookService(webhookRepository)
    }

    @Test
    fun `단위 - 요청 받은 웹훅을 저장되고 결과를 반환한다`() {
        // given
        val command =
            RegisterWebhookCommand(
                userId = 1L,
                type = WebhookType.SLACK,
                url = "https://hooks.slack.com/services/valid",
            )
        val saved =
            WebhookTestFactory.create(
                webhookId = 10L,
                userId = 1L,
                type = WebhookType.SLACK,
                url = command.url,
            )

        every { webhookRepository.save(any()) } returns saved

        // when
        val result = registerWebhookService.register(command)

        // then
        assertThat(result).isEqualTo(saved)
        verify(exactly = 1) { webhookRepository.save(any()) }
    }
}
