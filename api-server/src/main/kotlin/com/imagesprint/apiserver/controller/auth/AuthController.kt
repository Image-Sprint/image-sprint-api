package com.imagesprint.apiserver.controller.auth

import com.imagesprint.apiserver.controller.auth.dto.SocialLoginRequest
import com.imagesprint.apiserver.controller.auth.dto.SocialLoginResponse
import com.imagesprint.apiserver.controller.common.ApiResultResponse
import com.imagesprint.apiserver.controller.common.ApiVersions
import com.imagesprint.apiserver.controller.common.BaseController
import com.imagesprint.apiserver.security.AuthenticatedUser
import com.imagesprint.core.port.input.user.LogoutUserCase
import com.imagesprint.core.port.input.user.SocialLoginUseCase
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseCookie
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@RestController
@RequestMapping("${ApiVersions.V1}/auth")
class AuthController(
    private val socialLoginUseCase: SocialLoginUseCase,
    private val logoutUserCase: LogoutUserCase,
) : BaseController() {
    @PostMapping("/login")
    fun loginWithSocial(
        @RequestBody @Valid request: SocialLoginRequest,
        response: HttpServletResponse,
    ): ApiResultResponse<SocialLoginResponse> {
        val result = socialLoginUseCase.loginWithSocial(request.toCommand())

        val refreshTokenCookie =
            ResponseCookie
                .from("refreshToken", result.refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(30))
                .build()

        response.addHeader("Set-Cookie", refreshTokenCookie.toString())

        return ok(SocialLoginResponse(result.accessToken))
    }

    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        response: HttpServletResponse,
    ): ApiResultResponse<String> {
        logoutUserCase.logout(authenticatedUser.userId)

        val cookie = Cookie("refreshToken", "")
        cookie.maxAge = 0
        cookie.path = "/"
        cookie.isHttpOnly = true

        response.addCookie(cookie)

        return ApiResultResponse.ok("로그아웃 되었습니다.")
    }
}
