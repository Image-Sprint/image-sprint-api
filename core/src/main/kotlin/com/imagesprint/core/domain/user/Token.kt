package com.imagesprint.core.domain.user

data class Token(
    val tokenId: Long? = null,
    val userId: Long,
    val refreshToken: String,
) {
    fun removed(): Token = copy(refreshToken = "")

    fun refreshWith(newToken: String) = copy(refreshToken = newToken)
}
