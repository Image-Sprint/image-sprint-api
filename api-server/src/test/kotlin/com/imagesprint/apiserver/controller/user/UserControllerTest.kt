package com.imagesprint.apiserver.controller.user

import com.imagesprint.apiserver.support.WithMockAuthenticatedUser
import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.port.input.user.MyProfileResult
import com.imagesprint.core.port.input.user.UserQueryUseCase
import com.imagesprint.core.port.output.token.TokenProvider
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import kotlin.test.Test

@WebMvcTest(UserController::class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var userQueryUseCase: UserQueryUseCase

    @MockkBean
    private lateinit var tokenProvider: TokenProvider

    @Test
    @WithMockAuthenticatedUser(userId = 123, provider = "KAKAO")
    fun `유저 정보 조회 시 200 OK와 유저 정보를 반환한다`() {
        // given
        every { userQueryUseCase.getMyProfile(any()) } returns
            MyProfileResult(
                1L,
                "test",
                "test@test.com",
                SocialProvider.KAKAO,
                LocalDateTime.now(),
            )

        // when & then
        mockMvc
            .perform(
                get("/api/v1/users/me")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.userId").value(1L))
            .andExpect(jsonPath("$.data.nickname").value("test"))
            .andExpect(jsonPath("$.data.email").value("test@test.com"))
            .andDo(print())
    }
}
