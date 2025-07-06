package com.imagesprint.core.service.notification

import com.imagesprint.core.port.input.notification.GetNotificationPageQuery
import com.imagesprint.core.port.input.notification.NotificationPage
import com.imagesprint.core.port.input.notification.NotificationQueryUseCase
import com.imagesprint.core.port.output.notfication.NotificationRepository
import org.springframework.stereotype.Service

@Service
class NotificationQueryService(
    private val notificationRepository: NotificationRepository,
) : NotificationQueryUseCase {
    override fun getNotifications(query: GetNotificationPageQuery): NotificationPage =
        notificationRepository.getNotificationsByCursor(
            userId = query.userId,
            cursor = query.cursor,
            pageSize = query.pageSize,
        )
}
