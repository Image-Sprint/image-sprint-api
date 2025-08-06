package com.imagesprint.workerserver.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.imagesprint.core.domain.webhook.Webhook
import com.imagesprint.core.domain.webhook.WebhookLog
import com.imagesprint.core.port.output.webhook.ReactiveWebhookSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import software.amazon.awssdk.http.HttpStatusCode
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime

@Component
class DefaultWebhookSender(
    private val messageFactory: WebhookMessageFactory,
    private val objectMapper: ObjectMapper,
) : ReactiveWebhookSender {
    private val logger = LoggerFactory.getLogger(DefaultWebhookSender::class.java)
    private val httpClient: HttpClient = HttpClient.newHttpClient()

    override suspend fun send(
        webhook: Webhook,
        message: String,
    ): WebhookLog =
        try {
            val payloadMap = messageFactory.messageFor(webhook.type, message)
            val payloadJson = objectMapper.writeValueAsString(payloadMap)

            val request =
                HttpRequest
                    .newBuilder()
                    .uri(URI.create(webhook.url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
                    .build()

            val response =
                withContext(Dispatchers.IO) {
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                }

            WebhookLog(
                webhookId = webhook.webhookId!!,
                responseCode = response.statusCode(),
                responseMessage = response.body(),
                payload = message,
                isSuccess = response.statusCode() in 200..299,
                sentAt = LocalDateTime.now(),
            )
        } catch (e: Exception) {
            logger.error("[Webhook] Failed to send to ${webhook.url}", e)
            WebhookLog(
                webhookId = webhook.webhookId!!,
                responseCode = HttpStatusCode.INTERNAL_SERVER_ERROR,
                responseMessage = e.message ?: "Unknown error",
                payload = message,
                isSuccess = false,
                sentAt = LocalDateTime.now(),
            )
        }
}
