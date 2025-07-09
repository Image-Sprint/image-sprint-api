package com.imagesprint.core.port.output.webhook

import com.imagesprint.core.domain.webhook.Webhook

interface ReactiveWebhookRepository {
    suspend fun getForUser(userId: Long): List<Webhook>
}
