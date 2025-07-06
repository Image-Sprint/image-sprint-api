package com.imagesprint.core.port.input.webhook

import com.imagesprint.core.domain.webhook.WebhookType

data class RegisterWebhookCommand(
    val userId: Long,
    val type: WebhookType,
    val url: String,
)
