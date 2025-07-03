package com.imagesprint.apiserver.controller.auth.dto

import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.port.`in`.user.SocialAuthCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SocialLoginRequest(
    @field:NotBlank(message = "authorizationCode는 필수입니다.")
    val authorizationCode: String,
    @field:NotNull(message = "provider는 필수입니다.")
    val provider: SocialProvider,
    val state: String?
) {
    fun toCommand(): SocialAuthCommand {
        return SocialAuthCommand(
            authorizationCode = this.authorizationCode,
            provider = this.provider,
            state = this.state
        )
    }
}
