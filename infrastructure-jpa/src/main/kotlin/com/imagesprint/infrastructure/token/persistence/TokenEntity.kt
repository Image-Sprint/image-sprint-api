package com.imagesprint.infrastructure.token.persistence

import com.imagesprint.core.domain.user.Token
import com.imagesprint.infrastructure.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "token")
data class TokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val tokenId: Long? = null,
    val userId: Long,
    val refreshToken: String,
) : BaseTimeEntity() {
    fun toDomain(): Token =
        Token(
            tokenId = tokenId,
            userId = userId,
            refreshToken = refreshToken,
        )

    companion object {
        fun fromDomain(token: Token): TokenEntity =
            TokenEntity(
                tokenId = token.tokenId,
                userId = token.userId,
                refreshToken = token.refreshToken,
            )
    }
}
