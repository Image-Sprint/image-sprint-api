package com.imagesprint.core.domain.webhook

import java.time.LocalDateTime

data class Webhook(
    val webhookId: Long? = null,
    val userId: Long,
    val type: WebhookType,
    val url: String,
    val createdAt: LocalDateTime? = null,
)
