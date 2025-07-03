package com.imagesprint.core.port.out.user

interface SocialAuthPort {
    fun getUserInfo(authCode: String, state: String?): SocialUserInfo
}
