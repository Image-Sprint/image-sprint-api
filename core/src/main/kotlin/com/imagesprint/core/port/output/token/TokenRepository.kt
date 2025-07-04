package com.imagesprint.core.port.output.token

import com.imagesprint.core.domain.user.Token

interface TokenRepository {
    fun getRefreshToken(userId: Long): Token?

    fun save(token: Token)
}
