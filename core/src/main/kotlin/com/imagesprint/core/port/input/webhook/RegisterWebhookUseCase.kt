package com.imagesprint.core.port.input.webhook

import com.imagesprint.core.domain.webhook.Webhook

interface RegisterWebhookUseCase {
    fun register(command: RegisterWebhookCommand): Webhook
}
