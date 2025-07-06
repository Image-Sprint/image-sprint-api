package com.imagesprint.infrastructure.webhook.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface WebhookJpaRepository : JpaRepository<WebhookEntity, Long> {
    fun findAllByUserId(userId: Long): List<WebhookEntity>
}
