package com.imagesprint.infrastructure.webhook.persistence

import com.imagesprint.core.domain.webhook.Webhook
import com.imagesprint.core.port.output.webhook.WebhookRepository
import org.springframework.stereotype.Repository

@Repository
class WebhookRepositoryImpl(
    private val webhookJpaRepository: WebhookJpaRepository,
) : WebhookRepository {
    override fun getAllOf(userId: Long): List<Webhook> =
        webhookJpaRepository
            .findAllByUserId(userId)
            .map { w -> w.toDomain() }

    override fun save(webhook: Webhook): Webhook = webhookJpaRepository.save(WebhookEntity.fromDomain(webhook)).toDomain()
}
