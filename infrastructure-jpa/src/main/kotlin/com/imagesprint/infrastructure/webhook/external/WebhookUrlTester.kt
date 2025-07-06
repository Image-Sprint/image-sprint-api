package com.imagesprint.infrastructure.webhook.external

import com.imagesprint.core.domain.webhook.WebhookType
import com.imagesprint.core.exception.CustomException
import com.imagesprint.core.exception.ErrorCode
import com.imagesprint.core.port.output.webhook.WebhookUrlValidator
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class WebhookUrlTester(
    private val webClient: WebClient,
    private val messageFactory: WebhookMessageFactory,
) : WebhookUrlValidator {
    override fun validate(
        type: WebhookType,
        url: String,
    ) {
        try {
            val dummyPayload = messageFactory.dummyMessage(type)
            val response =
                webClient
                    .post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(dummyPayload)
                    .retrieve()
                    .toBodilessEntity()
                    .block()

            if (response?.statusCode?.is2xxSuccessful != true && response?.statusCode?.value() != 204) {
                throw CustomException(ErrorCode.INVALID_WEBHOOK_URL)
            }
        } catch (e: Exception) {
            throw CustomException(ErrorCode.WEBHOOK_REQUEST_FAILED)
        }
    }
}
