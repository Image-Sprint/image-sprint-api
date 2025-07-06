package com.imagesprint.apiserver.controller.webhook

import com.imagesprint.apiserver.controller.common.ApiResultResponse
import com.imagesprint.apiserver.controller.common.ApiResultResponse.Companion.ok
import com.imagesprint.apiserver.controller.common.ApiVersions
import com.imagesprint.apiserver.controller.webhook.dto.WebhookResponse
import com.imagesprint.apiserver.security.AuthenticatedUser
import com.imagesprint.core.port.input.webhook.GetWebhooksUseCase
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("${ApiVersions.V1}/webhooks")
class WebhookController(
    private val getWebhooksUseCase: GetWebhooksUseCase,
) {
    @GetMapping
    fun getWebhooks(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): ApiResultResponse<List<WebhookResponse>> {
        val result = getWebhooksUseCase.get(authenticatedUser.userId)

        return ok(result.map(WebhookResponse::from))
    }
}
