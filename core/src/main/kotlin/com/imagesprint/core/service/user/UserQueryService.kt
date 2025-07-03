package com.imagesprint.core.service.user

import com.imagesprint.core.domain.user.UserRepository
import com.imagesprint.core.exception.CustomException
import com.imagesprint.core.exception.ErrorCode
import com.imagesprint.core.port.input.user.MyProfileResult
import com.imagesprint.core.port.input.user.UserQueryUseCase
import org.springframework.stereotype.Service

@Service
class UserQueryService(
    private val userRepository: UserRepository,
) : UserQueryUseCase {
    override fun getMyProfile(userId: Long): MyProfileResult {
        val user = userRepository.getUser(userId) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        return MyProfileResult.from(user)
    }
}
