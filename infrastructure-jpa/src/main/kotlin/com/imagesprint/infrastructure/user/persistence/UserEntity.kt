package com.imagesprint.infrastructure.user.persistence

import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.domain.user.User
import com.imagesprint.infrastructure.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "`user`")
class UserEntity(
    email: String,
    provider: SocialProvider,
    nickname: String,
    isDeleted: Boolean = false,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userId: Long? = null

    @Column(nullable = false)
    var email: String = email
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var provider: SocialProvider = provider
        protected set

    @Column(nullable = false)
    var nickname: String = nickname
        protected set

    @Column
    var isDeleted: Boolean = isDeleted
        protected set

    fun toDomain(): User =
        User(
            userId = userId,
            email = email,
            provider = provider,
            nickname = nickname,
        )

    companion object {
        fun from(user: User): UserEntity =
            UserEntity(
                email = user.email,
                provider = user.provider,
                nickname = user.nickname,
                isDeleted = false,
            )
    }
}
