package com.imagesprint.core.service.user

import com.imagesprint.core.domain.user.Token
import com.imagesprint.core.port.output.token.TokenRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class LogoutServiceTest {
    private lateinit var tokenRepository: TokenRepository
    private lateinit var logoutService: LogoutService

    @BeforeEach
    fun setup() {
        tokenRepository = mockk()
        logoutService = LogoutService(tokenRepository)
    }

    @Test
    fun `단위 - 토큰이 존재하지 않으면 아무 작업도 하지 않는다`() {
        // given
        every { tokenRepository.getRefreshToken(1L) } returns null

        // when
        logoutService.logout(1L)

        // then
        verify(exactly = 1) { tokenRepository.getRefreshToken(1L) }
        verify(exactly = 0) { tokenRepository.save(any()) }
    }

    @Test
    fun `단위 - 토큰이 존재하면 refreshToken을 비우고 저장한다`() {
        // given
        val existingToken = Token(userId = 1L, refreshToken = "abc123")
        val removedToken = existingToken.removed()

        every { tokenRepository.getRefreshToken(1L) } returns existingToken
        every { tokenRepository.save(removedToken) } just Runs

        // when
        logoutService.logout(1L)

        // then
        verify(exactly = 1) { tokenRepository.getRefreshToken(1L) }
        verify(exactly = 1) { tokenRepository.save(removedToken) }
    }
}
