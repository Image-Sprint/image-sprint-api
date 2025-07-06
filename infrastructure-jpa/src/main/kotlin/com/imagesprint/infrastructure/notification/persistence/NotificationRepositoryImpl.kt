package com.imagesprint.infrastructure.notification.persistence

import com.imagesprint.core.domain.notification.Notification
import com.imagesprint.core.port.input.notification.NotificationPage
import com.imagesprint.core.port.output.notfication.NotificationRepository
import org.springframework.stereotype.Repository

@Repository
class NotificationRepositoryImpl(
    private val notificationJpaRepository: NotificationJpaRepository,
) : NotificationRepository {
    override fun getNotificationsByCursor(
        userId: Long,
        cursor: Long?,
        pageSize: Int,
    ): NotificationPage {
        val results = notificationJpaRepository.findByUserIdWithCursor(userId, cursor, pageSize)

        val hasNext = results.size > pageSize
        val pageItems = if (hasNext) results.dropLast(1) else results
        val nextCursor = if (hasNext) pageItems.lastOrNull()?.notificationId else null

        return NotificationPage(
            notifications = pageItems.map { it.toDomain() },
            nextCursor = nextCursor,
            hasNext = hasNext,
        )
    }

    override fun saveAll(notifications: List<Notification>) {
        notificationJpaRepository.saveAll(notifications.map(NotificationEntity::from))
    }
}
