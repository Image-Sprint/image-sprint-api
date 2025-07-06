package com.imagesprint.apiserver.integration

import com.imagesprint.apiserver.support.WithMockAuthenticatedUser
import com.imagesprint.core.domain.notification.Notification
import com.imagesprint.core.domain.notification.NotificationType
import com.imagesprint.core.port.output.notfication.NotificationRepository
import com.imagesprint.infrastructure.common.DatabaseCleaner
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.yml"])
@ActiveProfiles("test")
class NotificationIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var databaseCleaner: DatabaseCleaner

    @Autowired
    private lateinit var notificationRepository: NotificationRepository

    @BeforeEach
    fun setup() {
        databaseCleaner.truncate()

        val notifications =
            listOf(
                Notification(userId = 1L, content = "알림1", type = NotificationType.JOB_DONE),
                Notification(userId = 1L, content = "알림2", type = NotificationType.JOB_FAILED),
                Notification(userId = 1L, content = "알림3", type = NotificationType.ZIP_EXPIRED),
            )
        notificationRepository.saveAll(notifications)
    }

    @Test
    @WithMockAuthenticatedUser(userId = 1, provider = "KAKAO")
    fun `통합 - 알림 목록 조회 시 최근 순으로 페이징된 결과가 반환된다`() {
        // when & then
        mockMvc
            .get("/api/v1/notifications") {
                param("pageSize", "2")
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.notifications.size()") { value(2) }
                jsonPath("$.data.notifications[0].content") { value("알림3") }
                jsonPath("$.data.notifications[1].content") { value("알림2") }
                jsonPath("$.data.hasNext") { value(true) }
                jsonPath("$.data.nextCursor") { isNumber() }
            }
    }

    @Test
    @WithMockAuthenticatedUser(userId = 1, provider = "KAKAO")
    fun `통합 - 알림이 없으면 빈 목록이 반환된다`() {
        // given
        databaseCleaner.truncate()

        // when & then
        mockMvc
            .get("/api/v1/notifications") {
                param("pageSize", "10")
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.notifications") { isArray() }
                jsonPath("$.data.notifications.size()") { value(0) }
                jsonPath("$.data.hasNext") { value(false) }
                jsonPath("$.data.nextCursor") { isEmpty() }
            }
    }

    @Test
    @WithMockAuthenticatedUser(userId = 1, provider = "KAKAO")
    fun `통합 - 커서를 기준으로 다음 알림 페이지를 조회한다`() {
        // given
        val cursor = 2L

        // when & then
        mockMvc
            .get("/api/v1/notifications") {
                param("cursor", cursor.toString())
                param("pageSize", "10")
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.notifications.size()") { value(1) }
                jsonPath("$.data.notifications[0].content") { value("알림1") } // createdAt 기준 desc
                jsonPath("$.data.hasNext") { value(false) }
                jsonPath("$.data.nextCursor") { isEmpty() }
            }
    }
}
