package com.imagesprint.infrastructure.jpa.user.persistence

import com.imagesprint.core.domain.user.SocialProvider
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    fun findByEmailAndProvider(
        email: String,
        provider: SocialProvider,
    ): UserEntity?

    fun findByUserId(userId: Long): UserEntity?
}
