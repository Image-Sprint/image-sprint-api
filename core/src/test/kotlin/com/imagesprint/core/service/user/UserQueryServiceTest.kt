package com.imagesprint.core.service.user

import com.imagesprint.core.exception.CustomException
import com.imagesprint.core.exception.ErrorCode
import com.imagesprint.core.port.output.user.UserRepository
import com.imagesprint.core.support.factory.UserTestFactory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserQueryServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var userQueryService: UserQueryService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        userQueryService =
            UserQueryService(
                userRepository,
            )
    }

    @Test
    fun `유저 정보를 조회한다`() {
        // given
        val userId = 1L
        val returnUser = UserTestFactory.create(userId)
        every { userRepository.getUser(userId) } returns returnUser

        // when
        val result = userRepository.getUser(userId)

        // then
        assertThat(result?.userId).isEqualTo(userId)
    }

    @Test
    fun `유저 정보가 없으면 예외를 반환한다`() {
        // given
        val userId = 999L
        every { userRepository.getUser(any()) } throws CustomException(ErrorCode.USER_NOT_FOUND)

        // when & then
        assertThatThrownBy {
            userRepository.getUser(userId) // 이 부분이 예외 발생
        }.isInstanceOf(CustomException::class.java)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_NOT_FOUND)
    }
}
