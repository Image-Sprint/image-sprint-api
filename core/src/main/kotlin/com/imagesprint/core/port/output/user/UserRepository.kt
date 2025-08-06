package com.imagesprint.core.port.output.user

import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.domain.user.User

interface UserRepository {
    fun findBySocialIdentity(
        email: String,
        provider: SocialProvider,
    ): User?

    fun getUser(userId: Long): User?

    fun save(user: User): User
}
