package com.imagesprint.infrastructure.jpa.notification.persistence

import com.querydsl.jpa.impl.JPAQueryFactory

class NotificationQueryRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
) : NotificationQueryRepositoryCustom {
    override fun findByUserIdWithCursor(
        userId: Long,
        cursor: Long?,
        limit: Int,
    ): List<NotificationEntity> {
        val notification = QNotificationEntity.notificationEntity

        return queryFactory
            .selectFrom(notification)
            .where(
                notification.userId.eq(userId),
                cursor?.let { notification.notificationId.lt(it) }, // 커서 조건
            ).orderBy(notification.notificationId.desc())
            .limit((limit + 1).toLong()) // hasNext 판단 위해 +1
            .fetch()
    }
}
