package com.imagesprint.core.port.output.webhook

import com.imagesprint.core.domain.webhook.Webhook
import com.imagesprint.core.domain.webhook.WebhookLog

interface ReactiveWebhookSender {
    suspend fun send(
        webhook: Webhook,
        message: String,
    ): WebhookLog
}
