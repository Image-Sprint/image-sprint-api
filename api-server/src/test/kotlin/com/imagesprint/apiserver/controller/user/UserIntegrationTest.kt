package com.imagesprint.apiserver.controller.user

import com.imagesprint.apiserver.support.WithMockAuthenticatedUser
import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.domain.user.User
import com.imagesprint.core.domain.user.UserRepository
import com.imagesprint.infrastructure.common.DatabaseCleaner
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
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

    @BeforeEach
    fun setup() {
        databaseCleaner.truncate()
    }

    @WithMockAuthenticatedUser(userId = 1, provider = "KAKAO")
    @Test
    fun `유저 정보 조회 시, 유저 정보를 반환한다`() {
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
            .perform(
                get("/api/v1/users/me")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.userId").isNotEmpty)
            .andExpect(jsonPath("$.data.email").value("test@example.com"))
            .andExpect(jsonPath("$.data.nickname").value("test"))
    }

    @WithMockAuthenticatedUser(userId = 1, provider = "KAKAO")
    @Test
    fun `유저 정보 조회 시, 유저 정보가 없다면 예외를 반환한다`() {
        // given
        // 유저 저장 생략 -> DB에 존재하지 않음

        // when & then
        mockMvc
            .perform(
                get("/api/v1/users/me")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("유저를 찾을 수 없습니다."))
            .andExpect(jsonPath("$.data").doesNotExist())
    }
}
