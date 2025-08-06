package com.imagesprint.core.port.input.notification

import com.imagesprint.core.domain.notification.Notification

data class NotificationPage(
    val notifications: List<Notification>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)
