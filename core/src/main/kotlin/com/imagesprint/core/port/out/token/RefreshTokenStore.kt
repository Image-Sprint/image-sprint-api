package com.imagesprint.core.port.out.token

interface RefreshTokenStore {
    fun save(userId: Long, refreshToken: String)
}
