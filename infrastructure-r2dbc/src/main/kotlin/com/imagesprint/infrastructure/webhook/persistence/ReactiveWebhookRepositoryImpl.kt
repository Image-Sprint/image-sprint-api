package com.imagesprint.infrastructure.webhook.persistence

import com.imagesprint.core.domain.webhook.Webhook
import com.imagesprint.core.domain.webhook.WebhookType
import com.imagesprint.core.port.output.webhook.ReactiveWebhookRepository
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ReactiveWebhookRepositoryImpl(
    private val client: DatabaseClient,
) : ReactiveWebhookRepository {
    override suspend fun getForUser(userId: Long): List<Webhook> =
        client
            .sql(
                """
                SELECT webhook_id, user_id, type, url, created_at
                FROM webhook
                WHERE user_id = :userId
                """.trimIndent(),
            ).bind("userId", userId)
            .map(::toWebhook)
            .all()
            .collectList()
            .awaitSingle()

    private fun toWebhook(
        row: Row,
        meta: RowMetadata,
    ): Webhook =
        Webhook(
            webhookId = row.get("webhook_id", java.lang.Long::class.java)?.toLong(),
            userId = row.get("user_id", java.lang.Long::class.java)!!.toLong(),
            type = WebhookType.valueOf(row.get("type", String::class.java)!!),
            url = row.get("url", String::class.java)!!,
            createdAt = row.get("created_at", LocalDateTime::class.java),
        )
}
