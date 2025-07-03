package com.imagesprint.core.service.user

import com.imagesprint.core.domain.user.User
import com.imagesprint.core.domain.user.UserRepository
import com.imagesprint.core.port.input.user.SocialAuthCommand
import com.imagesprint.core.port.input.user.SocialLoginUseCase
import com.imagesprint.core.port.input.user.TokenResult
import com.imagesprint.core.port.output.token.RefreshTokenStore
import com.imagesprint.core.port.output.token.TokenProvider
import com.imagesprint.core.port.output.user.SocialAuthPortResolver
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SocialLoginService(
    private val userRepository: UserRepository,
    private val socialAuthPortResolver: SocialAuthPortResolver,
    private val tokenProvider: TokenProvider,
    private val refreshTokenStore: RefreshTokenStore,
) : SocialLoginUseCase {
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

        refreshTokenStore.save(userInfo.userId, refreshToken)

        return TokenResult(accessToken, refreshToken)
    }
}
