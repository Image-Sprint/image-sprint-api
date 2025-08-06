package com.imagesprint.apiserver.integration

import com.imagesprint.apiserver.support.WithMockAuthenticatedUser
import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.domain.user.User
import com.imagesprint.core.port.output.user.UserRepository
import com.imagesprint.infrastructure.jpa.common.DatabaseCleaner
import com.imagesprint.infrastructure.redis.subscriber.RedisJobProgressSubscriber
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.yml"])
@ActiveProfiles("test")
class UserIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var databaseCleaner: DatabaseCleaner

    @Autowired
    lateinit var userRepository: UserRepository

    @MockkBean
    lateinit var redisJobProgressSubscriber: RedisJobProgressSubscriber

    @BeforeEach
    fun setup() {
        databaseCleaner.truncate()
    }

    @WithMockAuthenticatedUser(userId = 1, provider = "KAKAO")
    @Test
    fun `통합 - 유저 정보 조회 시 200 OK와 유저 정보를 반환한다`() {
        // given
        userRepository.save(
            User(
                email = "test@example.com",
                provider = SocialProvider.KAKAO,
                nickname = "test",
            ),
        )

        // when & then
        mockMvc
            .get("/api/v1/users/me") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.userId") { exists() }
                jsonPath("$.data.email") { value("test@example.com") }
                jsonPath("$.data.nickname") { value("test") }
            }
    }

    @WithMockAuthenticatedUser(userId = 1, provider = "KAKAO")
    @Test
    fun `통합 - 유저 정보 조회 시, 유저 정보가 없다면 예외를 반환한다`() {
        // given
        // 유저 저장 생략

        // when & then
        mockMvc
            .get("/api/v1/users/me") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                jsonPath("$.message") { value("유저를 찾을 수 없습니다.") }
                jsonPath("$.data").doesNotExist()
            }
    }
}
