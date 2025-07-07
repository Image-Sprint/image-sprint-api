package com.imagesprint.core.service.user

import com.imagesprint.core.domain.user.Token
import com.imagesprint.core.domain.user.User
import com.imagesprint.core.port.input.user.SocialAuthCommand
import com.imagesprint.core.port.input.user.SocialLoginUseCase
import com.imagesprint.core.port.input.user.TokenResult
import com.imagesprint.core.port.output.token.TokenProvider
import com.imagesprint.core.port.output.token.TokenRepository
import com.imagesprint.core.port.output.user.SocialAuthPortResolver
import com.imagesprint.core.port.output.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SocialLoginService(
    private val userRepository: UserRepository,
    private val socialAuthPortResolver: SocialAuthPortResolver,
    private val tokenProvider: TokenProvider,
    private val tokenRepository: TokenRepository,
) : SocialLoginUseCase {
    /**
     * 소셜 로그인 플로우를 처리하는 메서드.
     *
     * 1. 소셜 인증 코드로 사용자 정보를 조회하고,
     * 2. 기존 유저가 없다면 회원가입을 진행하며,
     * 3. 액세스/리프레시 토큰을 발급 및 저장/갱신한다.
     *
     * @param command 소셜 인증 코드 및 provider 정보를 담은 커맨드
     * @return 발급된 액세스 토큰과 리프레시 토큰
     */
    @Transactional
    override fun loginWithSocial(command: SocialAuthCommand): TokenResult {
        val provider = socialAuthPortResolver.resolve(command.provider)
        val socialUserInfo = provider.getUserInfo(command.authorizationCode, command.state)
        val userInfo =
            userRepository.findBySocialIdentity(socialUserInfo.email, command.provider)
                ?: userRepository.save(
                    User(
                        email = socialUserInfo.email,
                        provider = command.provider,
                        nickname = socialUserInfo.nickname,
                    ),
                )

        val accessToken = tokenProvider.generateAccessToken(userInfo.userId!!, userInfo.provider.name)
        val refreshToken = tokenProvider.generateRefreshToken(userInfo.userId, userInfo.provider.name)

        val token =
            tokenRepository.getRefreshToken(userId = userInfo.userId)

        when {
            token == null -> {
                tokenRepository.save(Token(userId = userInfo.userId, refreshToken = refreshToken))
            }

            token.refreshToken.isBlank() -> {
                val refreshed = token.refreshWith(refreshToken)
                tokenRepository.save(refreshed)
            }
        }

        return TokenResult(accessToken, refreshToken)
    }
}
