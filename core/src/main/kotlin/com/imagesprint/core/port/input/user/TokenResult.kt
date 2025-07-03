package com.imagesprint.core.port.input.user

data class TokenResult(
    val accessToken: String,
    val refreshToken: String,
)
