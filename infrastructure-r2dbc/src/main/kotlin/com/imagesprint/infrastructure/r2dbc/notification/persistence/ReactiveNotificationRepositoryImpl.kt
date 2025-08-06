package com.imagesprint.infrastructure.r2dbc.notification.persistence

import com.imagesprint.core.domain.notification.Notification
import com.imagesprint.core.domain.notification.NotificationType
import com.imagesprint.core.port.output.notfication.ReactiveNotificationRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class ReactiveNotificationRepositoryImpl(
    private val client: DatabaseClient,
) : ReactiveNotificationRepository {
    override suspend fun save(notification: Notification) {
        client
            .sql(
                """
            INSERT INTO notification (user_id, content, type, created_at)
            VALUES (:userId, :content, :type, NOW())
            """,
            ).bind("userId", notification.userId)
            .bind("content", notification.content)
            .bind("type", notification.type.name)
            .then()
            .awaitSingleOrNull()
    }

    override suspend fun createJobStartedNotification(
        userId: Long,
        jobId: Long,
    ) {
        val content = "Job #$jobId 변환이 시작되었습니다."
        val notification =
            Notification(
                userId = userId,
                content = content,
                type = NotificationType.JOB_STARTED,
            )
        save(notification)
    }

    override suspend fun createJobFinishedNotification(
        userId: Long,
        jobId: Long,
        isSuccess: Boolean,
    ) {
        val content =
            if (isSuccess) {
                "Job #$jobId 이미지 변환이 완료되었습니다."
            } else {
                "Job #$jobId 처리 중 일부 이미지 변환에 실패하였습니다."
            }
        val notification =
            Notification(
                userId = userId,
                content = content,
                type = NotificationType.JOB_DONE,
            )
        save(notification)
    }
}
