package com.imagesprint.core.port.output.webhook

import com.imagesprint.core.domain.webhook.WebhookLog

interface ReactiveWebhookLogRepository {
    suspend fun save(log: WebhookLog)
}
