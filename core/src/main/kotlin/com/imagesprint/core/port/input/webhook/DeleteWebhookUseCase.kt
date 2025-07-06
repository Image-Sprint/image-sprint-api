package com.imagesprint.core.port.input.webhook

interface DeleteWebhookUseCase {
    fun delete(
        userId: Long,
        webhookId: Long,
    )
}
