package com.imagesprint.core.port.output.notfication

import com.imagesprint.core.domain.notification.Notification
import com.imagesprint.core.port.input.notification.NotificationPage

interface NotificationRepository {
    fun getNotificationsByCursor(
        userId: Long,
        cursor: Long?,
        pageSize: Int,
    ): NotificationPage

    fun saveAll(notifications: List<Notification>)
}
