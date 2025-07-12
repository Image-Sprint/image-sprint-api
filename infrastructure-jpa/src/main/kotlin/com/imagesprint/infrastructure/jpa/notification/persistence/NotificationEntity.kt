package com.imagesprint.infrastructure.jpa.notification.persistence

import com.imagesprint.core.domain.notification.Notification
import com.imagesprint.core.domain.notification.NotificationType
import com.imagesprint.infrastructure.jpa.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "notification")
class NotificationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val notificationId: Long? = null,
    @Column(nullable = false)
    val userId: Long,
    @Column(nullable = false)
    val content: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType,
) : BaseTimeEntity() {
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
                notificationId = null, // 생성 시에는 null
                userId = notification.userId,
                content = notification.content,
                type = notification.type,
            )
    }
}
