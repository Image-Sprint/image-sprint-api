package com.imagesprint.apiserver.support

import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.port.output.user.SocialAuthPort
import com.imagesprint.core.port.output.user.SocialAuthPortResolver
import com.imagesprint.core.port.output.user.SocialUserInfo
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class SocialAuthMockConfig {
    @Bean
    @Primary
    fun socialAuthPortResolver(): SocialAuthPortResolver =
        object : SocialAuthPortResolver {
            override fun resolve(provider: SocialProvider): SocialAuthPort =
                object : SocialAuthPort {
                    override fun getUserInfo(
                        authCode: String,
                        state: String?,
                    ): SocialUserInfo =
                        SocialUserInfo(
                            email = "mock@test.com",
                            nickname = "mockUser",
                        )
                }
        }
}
