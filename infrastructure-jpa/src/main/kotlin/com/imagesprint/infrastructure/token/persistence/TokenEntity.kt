package com.imagesprint.infrastructure.token.persistence

import com.imagesprint.core.domain.user.Token
import com.imagesprint.infrastructure.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "token")
class TokenEntity(
    userId: Long,
    refreshToken: String,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val tokenId: Long? = null

    @Column(nullable = false)
    var userId: Long = userId
        protected set

    @Column(nullable = false)
    var refreshToken: String = refreshToken
        protected set

    fun toDomain(): Token =
        Token(
            tokenId = tokenId,
            userId = userId,
            refreshToken = refreshToken,
        )

    companion object {
        fun from(token: Token): TokenEntity =
            TokenEntity(
                userId = token.userId,
                refreshToken = token.refreshToken,
            )
    }
}
