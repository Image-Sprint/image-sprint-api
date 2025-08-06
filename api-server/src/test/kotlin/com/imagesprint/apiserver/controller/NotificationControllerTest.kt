package com.imagesprint.apiserver.controller

import com.imagesprint.apiserver.controller.notification.NotificationController
import com.imagesprint.apiserver.security.AuthenticatedUser
import com.imagesprint.core.domain.notification.Notification
import com.imagesprint.core.domain.notification.NotificationType
import com.imagesprint.core.port.input.notification.GetNotificationPageQuery
import com.imagesprint.core.port.input.notification.NotificationPage
import com.imagesprint.core.port.input.notification.NotificationQueryUseCase
import com.imagesprint.core.port.output.token.TokenProvider
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDateTime
import kotlin.test.Test

@WebMvcTest(NotificationController::class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var notificationQueryUseCase: NotificationQueryUseCase

    @MockkBean
    private lateinit var tokenProvider: TokenProvider

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `컨트롤러 - 커서 없이 요청하면 첫 페이지를 조회한다`() {
        // given
        val userId = 1L
        val pageSize = 3
        val authenticatedUser = AuthenticatedUser(userId = userId, provider = "KAKAO")
        val notifications =
            listOf(
                Notification(
                    notificationId = 3L,
                    userId = 1L,
                    content = "알림1",
                    type = NotificationType.JOB_DONE,
                    createdAt = LocalDateTime.now(),
                ),
                Notification(
                    notificationId = 2L,
                    userId = 1L,
                    content = "알림2",
                    type = NotificationType.JOB_DONE,
                    createdAt = LocalDateTime.now(),
                ),
                Notification(
                    notificationId = 1L,
                    userId = 1L,
                    content = "알림3",
                    type = NotificationType.JOB_DONE,
                    createdAt = LocalDateTime.now(),
                ),
            )
        val page =
            NotificationPage(
                notifications = notifications,
                nextCursor = 1L,
                hasNext = true,
            )

        every {
            notificationQueryUseCase.getNotifications(GetNotificationPageQuery(userId, null, pageSize))
        } returns page

        val token = UsernamePasswordAuthenticationToken(authenticatedUser, null)
        SecurityContextHolder.getContext().authentication = token

        // when & then
        mockMvc
            .get("/api/v1/notifications") {
                param("pageSize", "$pageSize")
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.notifications.size()") { value(3) }
                jsonPath("$.data.nextCursor") { value(1L) }
                jsonPath("$.data.hasNext") { value(true) }
            }

        verify(exactly = 1) {
            notificationQueryUseCase.getNotifications(GetNotificationPageQuery(userId, null, pageSize))
        }
    }

    @Test
    fun `컨트롤러 - 알림 목록을 커서 기반으로 조회하면 200 OK와 결과를 반환한다`() {
        // given
        val authenticatedUser = AuthenticatedUser(userId = 1L, provider = "KAKAO")
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(authenticatedUser, null)

        val notification1 =
            Notification(
                notificationId = 100L,
                userId = 1L,
                content = "알림1",
                type = NotificationType.JOB_DONE,
                createdAt = LocalDateTime.now(),
            )

        val notification2 =
            Notification(
                notificationId = 99L,
                userId = 1L,
                content = "알림2",
                type = NotificationType.JOB_DONE,
                createdAt = LocalDateTime.now(),
            )

        val pageResult =
            NotificationPage(
                notifications = listOf(notification1, notification2),
                nextCursor = 99L,
                hasNext = true,
            )

        every {
            notificationQueryUseCase.getNotifications(
                GetNotificationPageQuery(userId = 1L, cursor = null, pageSize = 2),
            )
        } returns pageResult

        // when & then
        mockMvc
            .get("/api/v1/notifications") {
                contentType = MediaType.APPLICATION_JSON
                param("pageSize", "2")
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.notifications.size()") { value(2) }
                jsonPath("$.data.notifications[0].content") { value("알림1") }
                jsonPath("$.data.hasNext") { value(true) }
                jsonPath("$.data.nextCursor") { value(99) }
            }

        verify(exactly = 1) {
            notificationQueryUseCase.getNotifications(
                GetNotificationPageQuery(userId = 1L, cursor = null, pageSize = 2),
            )
        }
    }

    @Test
    fun `컨트롤러 - 알림이 없으면 빈 목록과 null 커서를 반환한다`() {
        // given
        val userId = 1L
        val pageSize = 10
        val authenticatedUser = AuthenticatedUser(userId = userId, provider = "KAKAO")
        val page =
            NotificationPage(
                notifications = emptyList(),
                nextCursor = null,
                hasNext = false,
            )

        every {
            notificationQueryUseCase.getNotifications(GetNotificationPageQuery(userId, null, pageSize))
        } returns page

        val token = UsernamePasswordAuthenticationToken(authenticatedUser, null)
        SecurityContextHolder.getContext().authentication = token

        // when & then
        mockMvc
            .get("/api/v1/notifications") {
                param("pageSize", "$pageSize")
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.notifications.size()") { value(0) }
                jsonPath("$.data.nextCursor") { doesNotExist() }
                jsonPath("$.data.hasNext") { value(false) }
            }

        verify(exactly = 1) {
            notificationQueryUseCase.getNotifications(GetNotificationPageQuery(userId, null, pageSize))
        }
    }
}
