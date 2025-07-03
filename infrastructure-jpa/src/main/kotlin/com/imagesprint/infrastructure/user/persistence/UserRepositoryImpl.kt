package com.imagesprint.infrastructure.user.persistence

import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.domain.user.User
import com.imagesprint.core.domain.user.UserRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {

    override fun findBySocialIdentity(email: String, provider: SocialProvider): User? {
        return userJpaRepository.findByEmailAndProvider(email, provider)?.toDomain()
    }

    override fun save(user: User): User {
        return userJpaRepository.save(UserEntity.fromDomain(user)).toDomain()
    }
}
