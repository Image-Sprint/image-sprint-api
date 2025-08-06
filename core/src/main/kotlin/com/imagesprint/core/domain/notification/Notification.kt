package com.imagesprint.core.domain.notification

import java.time.LocalDateTime

class Notification(
    val notificationId: Long? = null,
    val userId: Long,
    val content: String,
    val type: NotificationType,
    val createdAt: LocalDateTime? = null,
)
