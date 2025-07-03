package com.imagesprint.core.port.`in`.user

interface SocialLoginUseCase {
    fun socialAuthenticate(command: SocialAuthCommand): TokenResult
}
