package com.imagesprint.core.support.factory

import com.imagesprint.core.domain.notification.Notification
import com.imagesprint.core.domain.notification.NotificationType
import java.time.LocalDateTime

object NotificationTestFactory {
    fun create(
        id: Long? = null,
        userId: Long = 1L,
        content: String = "테스트 알림",
        type: NotificationType = NotificationType.JOB_DONE,
        createdAt: LocalDateTime = LocalDateTime.now(),
    ): Notification =
        Notification(
            notificationId = id,
            userId = userId,
            content = content,
            type = type,
            createdAt = createdAt,
        )
}
