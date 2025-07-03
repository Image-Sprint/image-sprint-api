package com.imagesprint.apiserver.controller.auth


import com.imagesprint.core.port.`in`.user.SocialLoginUseCase
import com.imagesprint.core.port.`in`.user.TokenResult
import com.imagesprint.core.port.out.token.TokenProvider
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@WebMvcTest(controllers = [AuthController::class], excludeAutoConfiguration = [SecurityAutoConfiguration::class])
@AutoConfigureMockMvc(addFilters = false) // 보안 필터 완전히 제거
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var socialLoginUseCase: SocialLoginUseCase

    @MockkBean
    private lateinit var tokenProvider: TokenProvider

    @Test
    fun `소셜 로그인 성공 시 200 OK와 액세스 토큰 반환한다`() {
        every { socialLoginUseCase.socialAuthenticate(any()) } returns TokenResult("access-token", "refresh-token")

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "authorizationCode": "auth-code",
                      "provider": "KAKAO",
                      "state": "state"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.accessToken").value("access-token"))
            .andDo(print())
    }
}
