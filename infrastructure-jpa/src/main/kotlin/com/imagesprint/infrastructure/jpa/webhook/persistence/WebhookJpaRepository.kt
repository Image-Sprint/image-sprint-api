package com.imagesprint.infrastructure.jpa.webhook.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface WebhookJpaRepository : JpaRepository<WebhookEntity, Long> {
    fun findAllByUserId(userId: Long): List<WebhookEntity>

    fun deleteByWebhookIdAndUserId(
        webhookId: Long,
        userId: Long,
    ): Int
}
