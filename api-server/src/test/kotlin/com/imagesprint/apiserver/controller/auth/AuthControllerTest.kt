package com.imagesprint.apiserver.controller.auth

import com.imagesprint.apiserver.security.AuthenticatedUser
import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.port.input.user.LogoutUserCase
import com.imagesprint.core.port.input.user.SocialLoginUseCase
import com.imagesprint.core.port.input.user.TokenResult
import com.imagesprint.core.port.output.token.TokenProvider
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.Test

@WebMvcTest(AuthController::class)
@AutoConfigureMockMvc(addFilters = false) // 보안 필터 완전히 제거
class AuthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var socialLoginUseCase: SocialLoginUseCase

    @MockkBean
    private lateinit var logoutUserCase: LogoutUserCase

    @MockkBean
    private lateinit var tokenProvider: TokenProvider

    @Test
    fun `소셜 로그인 성공 시 200 OK와 액세스 토큰 반환한다`() {
        // given
        val requestBody =
            """
            {
              "authorizationCode": "auth-code",
              "provider": "KAKAO",
              "state": "state"
            }
            """.trimIndent()
        every { socialLoginUseCase.loginWithSocial(any()) } returns TokenResult("access-token", "refresh-token")

        // when & then
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.accessToken").value("access-token"))
            .andDo(print())
    }

    @Test
    fun `로그아웃 성공 시 200 OK와 refreshToken 관련 쿠키를 제거한다`() {
        // given
        val authenticatedUser = AuthenticatedUser(userId = 1L, provider = SocialProvider.KAKAO.name)
        val authentication = UsernamePasswordAuthenticationToken(authenticatedUser, null)

        SecurityContextHolder.getContext().authentication = authentication

        every { logoutUserCase.logout(1L) } returns Unit

        // when & then
        mockMvc
            .perform(
                post("/api/v1/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value("로그아웃 되었습니다."))
            .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")))
            .andDo(print())

        verify(exactly = 1) { logoutUserCase.logout(1L) }

        SecurityContextHolder.clearContext()
    }
}
