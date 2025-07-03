package com.imagesprint.core.domain.user

data class User(
    val userId: Long? = null,
    val email: String,
    val provider: SocialProvider,
    val nickname: String,
    val isDeleted: Boolean = false
)
