package com.imagesprint.apiserver.controller.auth

import com.imagesprint.apiserver.security.AuthenticatedUser
import com.imagesprint.apiserver.support.SocialAuthMockConfig
import com.imagesprint.infrastructure.common.DatabaseCleaner
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@Import(SocialAuthMockConfig::class)
@ActiveProfiles("test")
class AuthIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var databaseCleaner: DatabaseCleaner

    @BeforeEach
    fun setup() {
        databaseCleaner.truncate()
    }

    @Test
    fun `로그인 요청 시 accessToken과 쿠키가 반환된다`() {
        // given
        val requestBody =
            """
            {
                "authorizationCode": "fake-code",
                "provider": "KAKAO",
                "state": "test"
            }
            """.trimIndent()

        // when & then
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty)
            .andExpect(header().exists("Set-Cookie"))
    }

    @Test
    fun `로그아웃 요청 시 refreshToken과 쿠키가 제거된다`() {
        // given
        val authenticatedUser = AuthenticatedUser(userId = 1L, provider = "KAKAO")
        val authentication = UsernamePasswordAuthenticationToken(authenticatedUser, null, emptyList())
        SecurityContextHolder.getContext().authentication = authentication

        // when & then
        mockMvc
            .perform(
                post("/api/v1/auth/logout")
                    .principal(authentication)
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value("로그아웃 되었습니다."))
            .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")))
    }
}
