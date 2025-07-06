package com.imagesprint.apiserver.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.imagesprint.apiserver.controller.auth.AuthController
import com.imagesprint.apiserver.controller.auth.dto.SocialLoginRequest
import com.imagesprint.apiserver.security.AuthenticatedUser
import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.port.input.user.LogoutUserCase
import com.imagesprint.core.port.input.user.SocialLoginUseCase
import com.imagesprint.core.port.input.user.TokenResult
import com.imagesprint.core.port.output.token.TokenProvider
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import kotlin.test.Test

@WebMvcTest(AuthController::class)
@AutoConfigureMockMvc(addFilters = false) // 보안 필터 완전히 제거
class AuthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var socialLoginUseCase: SocialLoginUseCase

    @MockkBean
    private lateinit var logoutUserCase: LogoutUserCase

    @MockkBean
    private lateinit var tokenProvider: TokenProvider

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `컨트롤러 - 소셜 로그인 성공 시 200 OK와 액세스 토큰 반환한다`() {
        // given
        val request =
            SocialLoginRequest(
                authorizationCode = "auth-code",
                provider = SocialProvider.KAKAO,
                state = "state",
            )

        every { socialLoginUseCase.loginWithSocial(any()) } returns TokenResult("access-token", "refresh-token")

        // when & then
        mockMvc
            .post("/api/v1/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.accessToken") { value("access-token") }
            }

        verify(exactly = 1) { socialLoginUseCase.loginWithSocial(any()) }
    }

    @Test
    fun `컨트롤러 - 로그아웃 성공 시 200 OK와 refreshToken 관련 쿠키를 제거한다`() {
        // given
        val authenticatedUser = AuthenticatedUser(userId = 1L, provider = SocialProvider.KAKAO.name)
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(authenticatedUser, null)

        every { logoutUserCase.logout(1L) } returns Unit

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

        verify(exactly = 1) { logoutUserCase.logout(1L) }
    }
}
