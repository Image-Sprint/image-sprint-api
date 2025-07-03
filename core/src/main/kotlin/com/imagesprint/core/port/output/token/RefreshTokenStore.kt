package com.imagesprint.core.port.output.token

interface RefreshTokenStore {
    fun save(
        userId: Long,
        refreshToken: String,
    )
}
