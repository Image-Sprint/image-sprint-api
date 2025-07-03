package com.imagesprint.core.port.out.token

interface TokenProvider {
    fun generateAccessToken(userId: Long, provider: String): String
    fun generateRefreshToken(userId: Long, provider: String): String
    fun isValidToken(token: String): Boolean
    fun getUserIdFromToken(token: String): Long
    fun getProviderFromToken(token: String): String
}
