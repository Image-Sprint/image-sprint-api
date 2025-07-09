package com.imagesprint.infrastructure.webhook.persistence

import com.imagesprint.core.domain.webhook.WebhookLog
import com.imagesprint.core.port.output.webhook.ReactiveWebhookLogRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class ReactiveWebhookLogRepositoryImpl(
    private val client: DatabaseClient,
) : ReactiveWebhookLogRepository {
    override suspend fun save(log: WebhookLog) {
        client
            .sql(
                """
            INSERT INTO webhook_log (webhook_id, response_code, response_message, payload, is_success, sent_at)
            VALUES (:webhookId, :responseCode, :responseMessage, :payload, :isSuccess, :sentAt)
            """,
            ).bind("webhookId", log.webhookId)
            .bind("responseCode", log.responseCode.name)
            .bind("responseMessage", log.responseMessage)
            .bind("payload", log.payload)
            .bind("isSuccess", log.isSuccess)
            .bind("sentAt", log.sentAt)
            .fetch()
            .rowsUpdated()
            .awaitSingleOrNull()
    }
}
