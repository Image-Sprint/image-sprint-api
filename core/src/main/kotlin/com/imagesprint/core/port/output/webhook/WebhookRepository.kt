package com.imagesprint.core.port.output.webhook

import com.imagesprint.core.domain.webhook.Webhook

interface WebhookRepository {
    fun getAllOf(userId: Long): List<Webhook>
}
