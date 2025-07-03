package com.imagesprint.infrastructure.token.persistence

import com.imagesprint.core.port.out.token.RefreshTokenStore
import org.springframework.stereotype.Repository

@Repository
class RefreshTokenStoreImpl(
    private val repository: TokenJpaRepository
) : RefreshTokenStore {
    override fun save(userId: Long, refreshToken: String) {
        repository.save(TokenEntity(userId = userId, refreshToken = refreshToken))
    }
}
