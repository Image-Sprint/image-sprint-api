package com.imagesprint.core.service.webhook

import com.imagesprint.core.domain.webhook.Webhook
import com.imagesprint.core.port.input.webhook.RegisterWebhookCommand
import com.imagesprint.core.port.input.webhook.RegisterWebhookUseCase
import com.imagesprint.core.port.output.webhook.WebhookRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Profile("api")
@Service
class RegisterWebhookService(
    private val webhookRepository: WebhookRepository,
) : RegisterWebhookUseCase {
    @Transactional
    override fun register(command: RegisterWebhookCommand): Webhook =
        webhookRepository.save(Webhook(userId = command.userId, type = command.type, url = command.url))
}
