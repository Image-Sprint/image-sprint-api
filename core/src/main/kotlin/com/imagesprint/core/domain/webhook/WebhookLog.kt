package com.imagesprint.core.domain.webhook

import java.time.LocalDateTime

data class WebhookLog(
    val webhookLogId: Long? = null,
    val webhookId: Long,
    val responseCode: WebhookType,
    val responseMessage: String,
    val payload: String,
    val isSuccess: Boolean,
    val sentAt: LocalDateTime? = null,
)
