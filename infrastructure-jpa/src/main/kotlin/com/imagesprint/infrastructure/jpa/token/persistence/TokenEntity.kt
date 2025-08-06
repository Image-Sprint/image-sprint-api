package com.imagesprint.infrastructure.jpa.token.persistence

import com.imagesprint.core.domain.user.Token
import com.imagesprint.infrastructure.jpa.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "token")
class TokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val tokenId: Long? = null,
    @Column(nullable = false)
    var userId: Long,
    @Column(nullable = false)
    var refreshToken: String,
) : BaseTimeEntity() {
    fun toDomain(): Token =
        Token(
            tokenId = tokenId,
            userId = userId,
            refreshToken = refreshToken,
        )

    companion object {
        fun from(token: Token): TokenEntity =
            TokenEntity(
                tokenId = token.tokenId,
                userId = token.userId,
                refreshToken = token.refreshToken,
            )
    }
}
