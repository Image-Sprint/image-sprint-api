package com.imagesprint.core.port.output.user

interface SocialAuthPort {
    fun getUserInfo(
        authCode: String,
        state: String?,
    ): SocialUserInfo
}
