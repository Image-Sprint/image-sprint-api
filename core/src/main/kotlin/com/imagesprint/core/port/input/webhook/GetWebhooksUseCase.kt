package com.imagesprint.core.port.input.webhook

import com.imagesprint.core.domain.webhook.Webhook

interface GetWebhooksUseCase {
    fun get(userId: Long): List<Webhook>
}
