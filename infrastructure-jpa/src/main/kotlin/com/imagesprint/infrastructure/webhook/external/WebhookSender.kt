package com.imagesprint.infrastructure.webhook.external

import com.imagesprint.core.domain.webhook.Webhook
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class WebhookSender(
    private val webClient: WebClient,
    private val messageFactory: WebhookMessageFactory,
) {
    fun send(
        webhook: Webhook,
        message: String,
    ) {
        val payload = messageFactory.messageFor(webhook.type, message)

        try {
            webClient
                .post()
                .uri(webhook.url)
                .header("Content-Type", "application/json")
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .block()
        } catch (e: Exception) {
            LoggerFactory.getLogger(javaClass).warn("웹훅 전송 실패: {}", e.message)
        }
    }
}
