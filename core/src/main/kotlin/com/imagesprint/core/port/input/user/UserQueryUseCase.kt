package com.imagesprint.core.port.input.user

interface UserQueryUseCase {
    fun getMyProfile(userId: Long): MyProfileResult
}
