package com.imagesprint.core.service.webhook

import com.imagesprint.core.domain.webhook.Webhook
import com.imagesprint.core.port.input.webhook.GetWebhooksUseCase
import com.imagesprint.core.port.output.webhook.WebhookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetWebhooksService(
    private val webhookRepository: WebhookRepository,
) : GetWebhooksUseCase {
    @Transactional(readOnly = true)
    override fun get(userId: Long): List<Webhook> = webhookRepository.getAllOf(userId)
}
