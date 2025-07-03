package com.imagesprint.core.domain.user

interface UserRepository {
    fun findBySocialIdentity(email: String, provider: SocialProvider): User?
    fun save(user: User): User
}
