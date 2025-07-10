package com.imagesprint.infrastructure.webhook.persistence

import com.imagesprint.core.domain.webhook.Webhook
import com.imagesprint.core.domain.webhook.WebhookType
import com.imagesprint.infrastructure.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "webhook")
class WebhookEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val webhookId: Long? = null,
    @Column(nullable = false)
    val userId: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: WebhookType,
    @Column(nullable = false)
    val url: String,
) : BaseTimeEntity() {
    fun toDomain(): Webhook =
        Webhook(
            webhookId = webhookId,
            userId = userId,
            type = type,
            url = url,
            createdAt = createdAt,
        )

    companion object {
        fun fromDomain(webhook: Webhook): WebhookEntity =
            WebhookEntity(
                webhookId = webhook.webhookId,
                userId = webhook.userId,
                type = webhook.type,
                url = webhook.url,
            )
    }
}
