package com.imagesprint.core.port.input.user

import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.domain.user.User
import java.time.LocalDateTime

data class MyProfileResult(
    val userId: Long,
    val nickname: String,
    val email: String,
    val provider: SocialProvider,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(user: User): MyProfileResult =
            MyProfileResult(
                requireNotNull(user.userId) { "유저 ID는 NULL 일 수 없습니다." },
                user.nickname,
                user.email,
                user.provider,
                user.createdAt ?: LocalDateTime.now(),
            )
    }
}
