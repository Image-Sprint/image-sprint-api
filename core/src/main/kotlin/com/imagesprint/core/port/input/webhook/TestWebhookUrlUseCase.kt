package com.imagesprint.core.port.input.webhook

import com.imagesprint.core.domain.webhook.WebhookType

interface TestWebhookUrlUseCase {
    fun test(
        type: WebhookType,
        url: String,
    )
}
