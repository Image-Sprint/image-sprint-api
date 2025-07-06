package com.imagesprint.core.port.input.notification

interface NotificationQueryUseCase {
    fun getNotifications(query: GetNotificationPageQuery): NotificationPage
}
