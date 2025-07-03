package com.imagesprint.core.service.user

import com.core.src.test.kotlin.com.imagesprint.core.support.factory.UserTestFactory
import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.domain.user.UserRepository
import com.imagesprint.core.port.`in`.user.SocialAuthCommand
import com.imagesprint.core.port.out.token.RefreshTokenStore
import com.imagesprint.core.port.out.token.TokenProvider
import com.imagesprint.core.port.out.user.SocialAuthPort
import com.imagesprint.core.port.out.user.SocialAuthPortResolver
import com.imagesprint.core.port.out.user.SocialUserInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SocialLoginServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var tokenProvider: TokenProvider
    private lateinit var refreshTokenStore: RefreshTokenStore
    private lateinit var socialAuthPortResolver: SocialAuthPortResolver
    private lateinit var socialAuthPort: SocialAuthPort
    private lateinit var socialLoginService: SocialLoginService

    @BeforeEach
    fun setup() {
        userRepository = mockk()
        tokenProvider = mockk()
        refreshTokenStore = mockk(relaxed = true)
        socialAuthPortResolver = mockk()
        socialAuthPort = mockk()
        socialLoginService = SocialLoginService(
            userRepository,
            socialAuthPortResolver,
            tokenProvider,
            refreshTokenStore
        )
    }

    @Test
    fun `유저가 존재하지 않을 경우, 유저를 저장하고 로그인 처리된다`() {
        // given
        val command = SocialAuthCommand("auth-code", SocialProvider.NAVER, "state")
        val socialUserInfo = SocialUserInfo(email = "test@example.com", nickname = "테스트")
        val savedUser = UserTestFactory.create(id = 1L, provider = SocialProvider.NAVER)

        every { socialAuthPortResolver.resolve(SocialProvider.NAVER) } returns socialAuthPort
        every { socialAuthPort.getUserInfo("auth-code", "state") } returns socialUserInfo
        every {
            userRepository.findBySocialIdentity(
                "test@example.com",
                SocialProvider.NAVER
            )
        } returns null
        every { userRepository.save(any()) } returns savedUser
        every { tokenProvider.generateAccessToken(1L, "NAVER") } returns "access-token"
        every { tokenProvider.generateRefreshToken(1L, "NAVER") } returns "refresh-token"

        // when
        val result = socialLoginService.socialAuthenticate(command)

        // then
        assertThat(result.accessToken).isEqualTo("access-token")
        assertThat(result.refreshToken).isEqualTo("refresh-token")

        verify(exactly = 1) {
            userRepository.save(match {
                it.email == "test@example.com" &&
                        it.nickname == "테스트" &&
                        it.provider == SocialProvider.NAVER
            })
        }
        verify { refreshTokenStore.save(1L, "refresh-token") }
    }

    @Test
    fun `이미 존재하는 유저일 경우 저장하지 않고 로그인 처리된다`() {
        // given
        val command = SocialAuthCommand("auth-code", SocialProvider.KAKAO, "state")
        val socialUserInfo = SocialUserInfo(email = "test@example.com", nickname = "테스트")
        val existingUser = UserTestFactory.create(id = 1L)

        every { socialAuthPortResolver.resolve(SocialProvider.KAKAO) } returns socialAuthPort
        every { socialAuthPort.getUserInfo("auth-code", "state") } returns socialUserInfo
        every { userRepository.findBySocialIdentity("test@example.com", SocialProvider.KAKAO) } returns existingUser
        every { tokenProvider.generateAccessToken(1L, "KAKAO") } returns "access-token"
        every { tokenProvider.generateRefreshToken(1L, "KAKAO") } returns "refresh-token"

        // when
        val result = socialLoginService.socialAuthenticate(command)

        // then
        assertThat(result.accessToken).isEqualTo("access-token")
        assertThat(result.refreshToken).isEqualTo("refresh-token")

        verify(exactly = 0) { userRepository.save(any()) }
        verify { refreshTokenStore.save(1L, "refresh-token") }
    }
}
