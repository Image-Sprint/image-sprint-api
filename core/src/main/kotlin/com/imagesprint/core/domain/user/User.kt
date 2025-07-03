package com.imagesprint.core.domain.user

import java.time.LocalDateTime

data class User(
    val userId: Long? = null,
    val email: String,
    val provider: SocialProvider,
    val nickname: String,
    val isDeleted: Boolean = false,
    val createdAt: LocalDateTime? = null,
)
