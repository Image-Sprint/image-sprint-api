package com.imagesprint.apiserver.controller.user

import com.imagesprint.apiserver.controller.common.ApiResultResponse
import com.imagesprint.apiserver.controller.common.ApiResultResponse.Companion.ok
import com.imagesprint.apiserver.controller.common.ApiVersions
import com.imagesprint.apiserver.controller.user.dto.MyProfileResponse
import com.imagesprint.apiserver.security.AuthenticatedUser
import com.imagesprint.core.port.input.user.UserQueryUseCase
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${ApiVersions.V1}/users")
class UserController(
    private val userQueryUseCase: UserQueryUseCase,
) {
    @GetMapping("/me")
    fun getMyProfile(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): ApiResultResponse<MyProfileResponse> {
        val result = userQueryUseCase.getMyProfile(authenticatedUser.userId)

        return ok(MyProfileResponse.from(result))
    }
}
