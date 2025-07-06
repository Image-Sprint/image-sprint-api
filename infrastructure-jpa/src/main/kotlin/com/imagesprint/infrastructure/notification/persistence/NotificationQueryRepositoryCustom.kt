package com.imagesprint.infrastructure.notification.persistence

interface NotificationQueryRepositoryCustom {
    fun findByUserIdWithCursor(
        userId: Long,
        cursor: Long?,
        limit: Int,
    ): List<NotificationEntity>
}
