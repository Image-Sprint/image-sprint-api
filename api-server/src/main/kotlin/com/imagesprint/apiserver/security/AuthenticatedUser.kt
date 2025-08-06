package com.imagesprint.apiserver.security

data class AuthenticatedUser(
    val userId: Long,
    val provider: String,
)
