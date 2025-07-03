package com.imagesprint.core.port.`in`.user

import com.imagesprint.core.domain.user.SocialProvider

data class SocialAuthCommand(
    val authorizationCode: String,
    val provider: SocialProvider,
    val state: String?
)
