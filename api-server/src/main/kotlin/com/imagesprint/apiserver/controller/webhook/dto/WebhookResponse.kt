package com.imagesprint.apiserver.controller.webhook.dto

import com.imagesprint.core.domain.webhook.Webhook
import com.imagesprint.core.domain.webhook.WebhookType

data class WebhookResponse(
    val webhookId: Long,
    val type: WebhookType,
    val url: String,
) {
    companion object {
        fun from(webhook: Webhook): WebhookResponse = WebhookResponse(webhook.webhookId!!, webhook.type, webhook.url)
    }
}
