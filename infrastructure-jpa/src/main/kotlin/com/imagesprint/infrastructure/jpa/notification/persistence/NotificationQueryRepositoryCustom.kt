package com.imagesprint.infrastructure.jpa.notification.persistence

interface NotificationQueryRepositoryCustom {
    fun findByUserIdWithCursor(
        userId: Long,
        cursor: Long?,
        limit: Int,
    ): List<NotificationEntity>
}
