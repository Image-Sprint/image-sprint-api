package com.imagesprint.core.port.output.notfication

import com.imagesprint.core.domain.notification.Notification

interface ReactiveNotificationRepository {
    suspend fun save(notification: Notification)

    suspend fun createJobStartedNotification(
        userId: Long,
        jobId: Long,
    )

    suspend fun createJobFinishedNotification(
        userId: Long,
        jobId: Long,
        isSuccess: Boolean,
    )
}
