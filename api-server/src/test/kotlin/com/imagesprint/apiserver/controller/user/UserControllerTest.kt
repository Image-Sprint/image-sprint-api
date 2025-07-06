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
import org.springframework.test.web.servlet.get
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
    fun `컨트롤러 - 유저 정보 조회 시 200 OK와 유저 정보를 반환한다`() {
        // given
        every { userQueryUseCase.getMyProfile(any()) } returns
            MyProfileResult(
                userId = 1L,
                nickname = "test",
                email = "test@test.com",
                provider = SocialProvider.KAKAO,
                createdAt = LocalDateTime.now(),
            )

        // when & then
        mockMvc
            .get("/api/v1/users/me") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.userId") { value(1L) }
                jsonPath("$.data.nickname") { value("test") }
                jsonPath("$.data.email") { value("test@test.com") }
            }.andDo {
                print()
            }
    }
}
