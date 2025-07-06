package com.imagesprint.apiserver.controller.webhook

import com.fasterxml.jackson.databind.ObjectMapper
import com.imagesprint.apiserver.controller.webhook.dto.RegisterWebhookRequest
import com.imagesprint.apiserver.controller.webhook.dto.TestWebhookUrlRequest
import com.imagesprint.apiserver.security.AuthenticatedUser
import com.imagesprint.core.domain.webhook.Webhook
import com.imagesprint.core.domain.webhook.WebhookType
import com.imagesprint.core.port.input.webhook.DeleteWebhookUseCase
import com.imagesprint.core.port.input.webhook.GetWebhooksUseCase
import com.imagesprint.core.port.input.webhook.RegisterWebhookUseCase
import com.imagesprint.core.port.input.webhook.TestWebhookUrlUseCase
import com.imagesprint.core.port.output.token.TokenProvider
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import kotlin.test.Test

@WebMvcTest(WebhookController::class)
@AutoConfigureMockMvc(addFilters = false)
class WebhookControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var getWebhooksUseCase: GetWebhooksUseCase

    @MockkBean
    private lateinit var registerWebhookUseCase: RegisterWebhookUseCase

    @MockkBean
    private lateinit var deleteWebhookUseCase: DeleteWebhookUseCase

    @MockkBean
    private lateinit var testWebhookUrlUseCase: TestWebhookUrlUseCase

    @MockkBean
    private lateinit var tokenProvider: TokenProvider

    private val userId = 1L

    @BeforeEach
    fun setup() {
        val user = AuthenticatedUser(userId = userId, provider = "KAKAO")
        val authentication = UsernamePasswordAuthenticationToken(user, null)
        SecurityContextHolder.getContext().authentication = authentication
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `컨트롤러 - 웹훅 등록 시 200 OK와 저장된 웹훅 정보를 반환한다`() {
        // given
        val request = RegisterWebhookRequest(WebhookType.SLACK, "https://slack.com/webhook")
        val command = request.toCommand(userId)
        val saved = Webhook(webhookId = 123L, userId, WebhookType.SLACK, request.url)

        every { registerWebhookUseCase.register(command) } returns saved

        // when & then
        mockMvc
            .post("/api/v1/webhooks") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.webhookId") { value(123L) }
                jsonPath("$.data.type") { value("SLACK") }
                jsonPath("$.data.url") { value("https://slack.com/webhook") }
            }

        verify(exactly = 1) { registerWebhookUseCase.register(command) }
    }

    @Test
    fun `컨트롤러 - 웹훅 전체 조회 시 200 OK와 목록을 반환한다`() {
        // given
        val webhookList =
            listOf(
                Webhook(webhookId = 1L, userId, WebhookType.DISCORD, "https://discord.com/webhook"),
                Webhook(webhookId = 2L, userId, WebhookType.SLACK, "https://slack.com/webhook"),
            )

        every { getWebhooksUseCase.get(userId) } returns webhookList

        // when & then
        mockMvc
            .get("/api/v1/webhooks")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.size()") { value(2) }
                jsonPath("$.data[0].type") { value("DISCORD") }
                jsonPath("$.data[1].type") { value("SLACK") }
            }

        verify(exactly = 1) { getWebhooksUseCase.get(userId) }
    }

    @Test
    fun `컨트롤러 - 웹훅 URL 테스트 시 200 OK 반환한다`() {
        // given
        val request = TestWebhookUrlRequest(WebhookType.SLACK, "https://slack.com/webhook")
        every { testWebhookUrlUseCase.test(request.type, request.url) } returns Unit

        // when & then
        mockMvc
            .post("/api/v1/webhooks/test") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.data") { value("올바른 url입니다.") }
            }

        verify(exactly = 1) { testWebhookUrlUseCase.test(request.type, request.url) }
    }

    @Test
    fun `컨트롤러 - 웹훅 삭제 요청 시 200 OK와 메시지를 반환한다`() {
        // given
        val webhookId = 123L
        every { deleteWebhookUseCase.delete(userId, webhookId) } returns Unit

        // when & then
        mockMvc
            .delete("/api/v1/webhooks/$webhookId")
            .andExpect {
                status { isOk() }
                jsonPath("$.data") { value("등록된 웹훅이 삭제되었습니다.") }
            }

        verify(exactly = 1) { deleteWebhookUseCase.delete(userId, webhookId) }
    }
}
