package com.imagesprint.infrastructure.notification.persistence

import com.imagesprint.core.domain.notification.Notification
import com.imagesprint.core.domain.notification.NotificationType
import com.imagesprint.infrastructure.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "notification")
class NotificationEntity(
    userId: Long,
    content: String,
    type: NotificationType,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val notificationId: Long? = null

    @Column(nullable = false)
    var userId: Long = userId
        protected set

    @Column(nullable = false)
    var content: String = content
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: NotificationType = type
        protected set

    fun toDomain(): Notification =
        Notification(
            notificationId = notificationId,
            userId = userId,
            content = content,
            type = type,
            createdAt = createdAt,
        )

    companion object {
        fun from(notification: Notification): NotificationEntity =
            NotificationEntity(
                userId = notification.userId,
                content = notification.content,
                type = notification.type,
            )
    }
}
