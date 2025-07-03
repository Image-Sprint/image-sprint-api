package com.imagesprint.infrastructure.user.persistence

import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.domain.user.User
import com.imagesprint.infrastructure.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "user")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userId: Long? = null,

    val email: String,

    @Enumerated(EnumType.STRING)
    val provider: SocialProvider,

    val nickname: String,

    val isDeleted: Boolean = false,
) : BaseTimeEntity() {

    fun toDomain(): User = User(
        userId = userId,
        email = email,
        provider = provider,
        nickname = nickname
    )

    companion object {
        fun fromDomain(user: User): UserEntity =
            UserEntity(
                userId = user.userId,
                email = user.email,
                provider = user.provider,
                nickname = user.nickname
            )
    }
}
