package com.imagesprint.core.support.factory

import com.imagesprint.core.domain.webhook.Webhook
import com.imagesprint.core.domain.webhook.WebhookType

object WebhookTestFactory {
    fun create(
        webhookId: Long? = null,
        userId: Long = 1L,
        type: WebhookType = WebhookType.SLACK,
        url: String = "https://hooks.slack.com/services/test",
    ): Webhook =
        Webhook(
            webhookId = webhookId,
            userId = userId,
            type = type,
            url = url,
        )
}
