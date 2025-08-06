package com.imagesprint.apiserver.controller.webhook.dto

import com.imagesprint.core.domain.webhook.WebhookType
import com.imagesprint.core.port.input.webhook.RegisterWebhookCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class RegisterWebhookRequest(
    @field:NotNull(message = "웹훅 타입은 필수입니다.")
    val type: WebhookType,
    @field:NotBlank(message = "URL은 필수입니다.")
    val url: String,
) {
    fun toCommand(userId: Long): RegisterWebhookCommand =
        RegisterWebhookCommand(
            userId,
            type,
            url,
        )
}
