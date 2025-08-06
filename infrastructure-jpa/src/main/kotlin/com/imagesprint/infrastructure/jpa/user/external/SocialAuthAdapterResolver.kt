package com.imagesprint.infrastructure.jpa.user.external

import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.port.output.user.SocialAuthPort
import com.imagesprint.core.port.output.user.SocialAuthPortResolver
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class SocialAuthAdapterResolver(
    @Qualifier("KAKAO") private val kakaoAdapter: SocialAuthPort,
    @Qualifier("NAVER") private val naverAdapter: SocialAuthPort,
) : SocialAuthPortResolver {
    override fun resolve(provider: SocialProvider): SocialAuthPort =
        when (provider) {
            SocialProvider.KAKAO -> kakaoAdapter
            SocialProvider.NAVER -> naverAdapter
        }
}
