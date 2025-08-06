package com.imagesprint.core.port.output.webhook

import com.imagesprint.core.domain.webhook.Webhook

interface WebhookSender {
    fun send(
        webhook: Webhook,
        message: String,
    )
}
