package com.imagesprint.infrastructure.user.persistence

import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.domain.user.User
import com.imagesprint.infrastructure.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "`user`")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userId: Long? = null,
    @Column(nullable = false)
    val email: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: SocialProvider,
    @Column(nullable = false)
    val nickname: String,
    @Column(nullable = false)
    val isDeleted: Boolean = false,
) : BaseTimeEntity() {
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
                userId = null, // 새로 생성할 때는 null
                email = user.email,
                provider = user.provider,
                nickname = user.nickname,
                isDeleted = false,
            )
    }
}
