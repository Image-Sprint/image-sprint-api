package com.imagesprint.core.service.user

import com.imagesprint.core.exception.CustomException
import com.imagesprint.core.exception.ErrorCode
import com.imagesprint.core.port.input.user.MyProfileResult
import com.imagesprint.core.port.input.user.UserQueryUseCase
import com.imagesprint.core.port.output.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserQueryService(
    private val userRepository: UserRepository,
) : UserQueryUseCase {
    @Transactional(readOnly = true)
    override fun getMyProfile(userId: Long): MyProfileResult {
        val user = userRepository.getUser(userId) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        return MyProfileResult.from(user)
    }
}
