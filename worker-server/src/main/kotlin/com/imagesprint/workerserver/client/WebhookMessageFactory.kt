package com.imagesprint.workerserver.client

import com.imagesprint.core.domain.webhook.WebhookType
import org.springframework.stereotype.Component

@Component
class WebhookMessageFactory {
    fun messageFor(
        type: WebhookType,
        message: String,
    ): Map<String, Any> =
        when (type) {
            WebhookType.SLACK -> mapOf("text" to message)
            WebhookType.DISCORD -> mapOf("content" to message)
        }
}
