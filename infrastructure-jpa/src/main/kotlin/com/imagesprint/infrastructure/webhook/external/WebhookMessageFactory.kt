package com.imagesprint.infrastructure.webhook.external

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

    fun dummyMessage(type: WebhookType): Map<String, Any> =
        when (type) {
            WebhookType.SLACK -> mapOf("text" to "[TEST] 슬랙 웹훅 유효성 검사입니다.")
            WebhookType.DISCORD -> mapOf("content" to "[TEST] 디스코드 웹훅 유효성 검사입니다.")
        }
}
