package com.imagesprint.apiserver.controller.user.dto

import com.imagesprint.core.port.input.notification.NotificationPage

data class NotificationPageResponse(
    val notifications: List<NotificationResponse>,
    val nextCursor: Long?,
    val hasNext: Boolean,
) {
    companion object {
        fun from(page: NotificationPage): NotificationPageResponse =
            NotificationPageResponse(
                notifications = page.notifications.map { NotificationResponse.from(it) },
                nextCursor = page.nextCursor,
                hasNext = page.hasNext,
            )
    }
}
