package com.imagesprint.core.service.webhook

import com.imagesprint.core.exception.CustomException
import com.imagesprint.core.exception.ErrorCode
import com.imagesprint.core.port.input.webhook.DeleteWebhookUseCase
import com.imagesprint.core.port.output.webhook.WebhookRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Profile("api")
@Service
class DeleteWebhookService(
    private val webhookRepository: WebhookRepository,
) : DeleteWebhookUseCase {
    @Transactional
    override fun delete(
        userId: Long,
        webhookId: Long,
    ) {
        val deleted = webhookRepository.removeOwnedBy(userId, webhookId)

        if (!deleted) {
            throw CustomException(ErrorCode.WEBHOOK_NOT_FOUND)
        }
    }
}
