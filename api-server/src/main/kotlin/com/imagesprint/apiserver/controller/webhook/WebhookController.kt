package com.imagesprint.apiserver.controller.webhook

import com.imagesprint.apiserver.controller.common.ApiResultResponse
import com.imagesprint.apiserver.controller.common.ApiResultResponse.Companion.ok
import com.imagesprint.apiserver.controller.common.ApiVersions
import com.imagesprint.apiserver.controller.webhook.dto.RegisterWebhookRequest
import com.imagesprint.apiserver.controller.webhook.dto.TestWebhookUrlRequest
import com.imagesprint.apiserver.controller.webhook.dto.WebhookResponse
import com.imagesprint.apiserver.security.AuthenticatedUser
import com.imagesprint.core.port.input.webhook.DeleteWebhookUseCase
import com.imagesprint.core.port.input.webhook.GetWebhooksUseCase
import com.imagesprint.core.port.input.webhook.RegisterWebhookUseCase
import com.imagesprint.core.port.input.webhook.TestWebhookUrlUseCase
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("${ApiVersions.V1}/webhooks")
class WebhookController(
    private val getWebhooksUseCase: GetWebhooksUseCase,
    private val registerWebhookUseCase: RegisterWebhookUseCase,
    private val testWebhookUrlUseCase: TestWebhookUrlUseCase,
    private val deleteWebhookUseCase: DeleteWebhookUseCase,
) {
    @GetMapping
    fun getWebhooks(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): ApiResultResponse<List<WebhookResponse>> {
        val result = getWebhooksUseCase.get(authenticatedUser.userId)

        return ok(result.map(WebhookResponse::from))
    }

    @PostMapping
    fun register(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @RequestBody @Valid request: RegisterWebhookRequest,
    ): ApiResultResponse<WebhookResponse> {
        val command = request.toCommand(authenticatedUser.userId)
        val result = registerWebhookUseCase.register(command)

        return ok(WebhookResponse.from(result))
    }

    @PostMapping("/test")
    fun test(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @RequestBody @Valid request: TestWebhookUrlRequest,
    ): ApiResultResponse<String> {
        testWebhookUrlUseCase.test(request.type, request.url)

        return ok("올바른 url입니다.")
    }

    @DeleteMapping("/{webhookId}")
    fun delete(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @PathVariable webhookId: Long,
    ): ApiResultResponse<String> {
        deleteWebhookUseCase.delete(authenticatedUser.userId, webhookId)

        return ok("등록된 웹훅이 삭제되었습니다.")
    }
}
