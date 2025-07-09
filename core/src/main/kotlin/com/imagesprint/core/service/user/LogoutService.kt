package com.imagesprint.core.service.user

import com.imagesprint.core.port.input.user.LogoutUserCase
import com.imagesprint.core.port.output.token.TokenRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Profile("api")
@Service
class LogoutService(
    private val tokenRepository: TokenRepository,
) : LogoutUserCase {
    @Transactional
    override fun logout(userId: Long) {
        val token = tokenRepository.getRefreshToken(userId) ?: return

        val removed = token.removed()

        tokenRepository.save(removed)
    }
}
