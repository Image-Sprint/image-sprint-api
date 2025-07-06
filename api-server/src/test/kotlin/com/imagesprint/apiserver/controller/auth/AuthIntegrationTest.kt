package com.imagesprint.apiserver.controller.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.imagesprint.apiserver.controller.auth.dto.SocialLoginRequest
import com.imagesprint.apiserver.security.AuthenticatedUser
import com.imagesprint.apiserver.support.SocialAuthMockConfig
import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.infrastructure.common.DatabaseCleaner
import org.hamcrest.CoreMatchers.containsString
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
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@Import(SocialAuthMockConfig::class)
@ActiveProfiles("test")
class AuthIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var databaseCleaner: DatabaseCleaner

    @BeforeEach
    fun setup() {
        databaseCleaner.truncate()
    }

    @Test
    fun `통합 - 로그인 요청 시 accessToken과 쿠키가 반환된다`() {
        // given
        val request =
            SocialLoginRequest(
                authorizationCode = "auth-code",
                provider = SocialProvider.KAKAO,
                state = "state",
            )

        // when & then
        mockMvc
            .post("/api/v1/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.accessToken").isNotEmpty
                header().exists("Set-Cookie")
            }
    }

    @Test
    fun `통합 - 로그아웃 요청 시 refreshToken과 쿠키가 제거된다`() {
        // given
        val authenticatedUser = AuthenticatedUser(userId = 1L, provider = "KAKAO")
        val authentication = UsernamePasswordAuthenticationToken(authenticatedUser, null, emptyList())
        SecurityContextHolder.getContext().authentication = authentication

        // when & then
        mockMvc
            .post("/api/v1/auth/logout") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                jsonPath("$.data") { value("로그아웃 되었습니다.") }
                header { string("Set-Cookie", containsString("Max-Age=0")) }
            }.andDo {
                print()
            }
    }
}
