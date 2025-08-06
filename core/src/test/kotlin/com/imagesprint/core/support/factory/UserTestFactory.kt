package com.imagesprint.core.support.factory

import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.domain.user.User

object UserTestFactory {
    fun create(
        id: Long? = null,
        email: String = "test@example.com",
        provider: SocialProvider = SocialProvider.KAKAO,
        nickname: String = "테스트",
    ): User =
        User(
            userId = id,
            email = email,
            provider = provider,
            nickname = nickname,
        )
}
