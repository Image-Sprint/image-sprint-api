package com.imagesprint.apiserver.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.imagesprint.apiserver.controller.webhook.dto.RegisterWebhookRequest
import com.imagesprint.apiserver.support.WithMockAuthenticatedUser
import com.imagesprint.core.domain.webhook.Webhook
import com.imagesprint.core.domain.webhook.WebhookType
import com.imagesprint.core.port.output.webhook.WebhookRepository
import com.imagesprint.infrastructure.common.DatabaseCleaner
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.yml"])
@ActiveProfiles("test")
class WebhookIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var webhookRepository: WebhookRepository

    @Autowired
    private lateinit var databaseCleaner: DatabaseCleaner

    @BeforeEach
    fun setup() {
        databaseCleaner.truncate()
    }

    @Test
    @WithMockAuthenticatedUser(userId = 1, provider = "KAKAO")
    fun `통합 - 웹훅 등록 시 200 OK와 저장된 웹훅 정보를 반환한다`() {
        // given
        val request = RegisterWebhookRequest(WebhookType.SLACK, "https://slack.com/webhook")

        // when & then
        mockMvc
            .post("/api/v1/webhooks") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.webhookId") { exists() }
                jsonPath("$.data.type") { value("SLACK") }
                jsonPath("$.data.url") { value("https://slack.com/webhook") }
            }
    }

    @Test
    @WithMockAuthenticatedUser(userId = 1, provider = "KAKAO")
    fun `통합 - 웹훅 목록 조회 시 200 OK와 등록된 웹훅 리스트를 반환한다`() {
        // given
        val webhook =
            webhookRepository.save(
                Webhook(null, 1L, WebhookType.DISCORD, "https://discord.com/webhook"),
            )

        // when & then
        mockMvc
            .get("/api/v1/webhooks") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                jsonPath("$.data[0].webhookId") { value(webhook.webhookId!!) }
                jsonPath("$.data[0].type") { value("DISCORD") }
                jsonPath("$.data[0].url") { value("https://discord.com/webhook") }
            }
    }

    @Test
    @WithMockAuthenticatedUser(userId = 1, provider = "KAKAO")
    fun `통합 - 웹훅 삭제 시 200 OK를 반환한다`() {
        // given
        val webhook =
            webhookRepository.save(
                Webhook(null, 1L, WebhookType.SLACK, "https://slack.com/delete-me"),
            )

        // when & then
        mockMvc
            .delete("/api/v1/webhooks/${webhook.webhookId}") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                jsonPath("$.data") { value("등록된 웹훅이 삭제되었습니다.") }
            }
    }
}
