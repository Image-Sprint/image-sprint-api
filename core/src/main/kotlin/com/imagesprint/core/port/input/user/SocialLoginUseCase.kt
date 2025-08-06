package com.imagesprint.core.port.input.user

interface SocialLoginUseCase {
    fun loginWithSocial(command: SocialAuthCommand): TokenResult
}
