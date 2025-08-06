package com.imagesprint.core.service.notification

import com.imagesprint.core.domain.notification.NotificationType
import com.imagesprint.core.port.input.notification.GetNotificationPageQuery
import com.imagesprint.core.port.input.notification.NotificationPage
import com.imagesprint.core.port.output.notfication.NotificationRepository
import com.imagesprint.core.support.factory.NotificationTestFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import java.time.LocalDateTime
import kotlin.test.Test

class NotificationQueryServiceTest {
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var notificationQueryService: NotificationQueryService

    @BeforeEach
    fun setup() {
        notificationRepository = mockk()
        notificationQueryService = NotificationQueryService(notificationRepository)
    }

    @Test
    fun `단위 - 알림 목록을 정상적으로 조회한다`() {
        // given
        val query =
            GetNotificationPageQuery(
                userId = 1L,
                cursor = null,
                pageSize = 3,
            )

        val now = LocalDateTime.now()
        val notifications =
            listOf(
                NotificationTestFactory.create(
                    id = 101L,
                    userId = 1L,
                    content = "알림1",
                    type = NotificationType.JOB_FAILED,
                    createdAt = now,
                ),
                NotificationTestFactory.create(
                    id = 100L,
                    userId = 1L,
                    content = "알림2",
                    type = NotificationType.JOB_DONE,
                    createdAt = now.minusSeconds(1),
                ),
                NotificationTestFactory.create(
                    id = 99L,
                    userId = 1L,
                    content = "알림3",
                    type = NotificationType.JOB_DONE,
                    createdAt = now.minusSeconds(2),
                ),
            )

        val expected =
            NotificationPage(
                notifications = notifications,
                nextCursor = 99L,
                hasNext = true,
            )

        every {
            notificationRepository.getNotificationsByCursor(
                userId = 1L,
                cursor = null,
                pageSize = 3,
            )
        } returns expected

        // when
        val result = notificationQueryService.getNotifications(query)

        // then
        assertThat(result).isEqualTo(expected)
        assertThat(result.notifications).hasSize(3)
        verify(exactly = 1) {
            notificationRepository.getNotificationsByCursor(
                userId = 1L,
                cursor = null,
                pageSize = 3,
            )
        }
    }
}
