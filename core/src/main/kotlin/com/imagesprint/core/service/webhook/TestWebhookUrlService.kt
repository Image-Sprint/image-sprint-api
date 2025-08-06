package com.imagesprint.core.service.webhook

import com.imagesprint.core.domain.webhook.WebhookType
import com.imagesprint.core.port.input.webhook.TestWebhookUrlUseCase
import com.imagesprint.core.port.output.webhook.WebhookUrlValidator
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("api")
@Service
class TestWebhookUrlService(
    private val validator: WebhookUrlValidator,
) : TestWebhookUrlUseCase {
    override fun test(
        type: WebhookType,
        url: String,
    ) {
        validator.validate(type, url)
    }
}
