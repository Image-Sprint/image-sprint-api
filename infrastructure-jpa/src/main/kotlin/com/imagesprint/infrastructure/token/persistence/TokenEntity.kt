package com.imagesprint.infrastructure.token.persistence

import com.imagesprint.infrastructure.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "token")
data class TokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val tokenId: Long? = null,

    val userId: Long,

    val refreshToken: String
) : BaseTimeEntity()
