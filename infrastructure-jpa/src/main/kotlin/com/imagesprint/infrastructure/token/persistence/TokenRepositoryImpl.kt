package com.imagesprint.infrastructure.token.persistence

import com.imagesprint.core.domain.user.Token
import com.imagesprint.core.port.output.token.TokenRepository
import org.springframework.stereotype.Repository

@Repository
class TokenRepositoryImpl(
    private val repository: TokenJpaRepository,
) : TokenRepository {
    override fun getRefreshToken(userId: Long): Token? = repository.getByUserId(userId)?.toDomain()

    override fun save(token: Token) {
        repository.save(TokenEntity.from(token))
    }
}
