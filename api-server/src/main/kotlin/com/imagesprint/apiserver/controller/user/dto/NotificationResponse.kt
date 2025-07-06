package com.imagesprint.apiserver.controller.user.dto

import com.imagesprint.core.domain.notification.Notification
import com.imagesprint.core.domain.notification.NotificationType
import java.time.LocalDateTime

data class NotificationResponse(
    val id: Long,
    val content: String,
    val type: NotificationType,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(notification: Notification): NotificationResponse =
            NotificationResponse(
                id = notification.notificationId!!,
                content = notification.content,
                type = notification.type,
                createdAt = notification.createdAt!!,
            )
    }
}
