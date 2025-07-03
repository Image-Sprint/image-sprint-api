package com.imagesprint.apiserver.controller.user.dto

import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.port.input.user.MyProfileResult
import java.time.LocalDateTime

data class MyProfileResponse(
    val userId: Long,
    val nickname: String,
    val email: String,
    val provider: SocialProvider,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(result: MyProfileResult): MyProfileResponse =
            MyProfileResponse(
                result.userId,
                result.nickname,
                result.email,
                result.provider,
                result.createdAt,
            )
    }
}
