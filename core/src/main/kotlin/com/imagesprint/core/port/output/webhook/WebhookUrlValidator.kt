package com.imagesprint.core.port.output.webhook

import com.imagesprint.core.domain.webhook.WebhookType

interface WebhookUrlValidator {
    fun validate(
        type: WebhookType,
        url: String,
    )
}
