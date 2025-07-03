package com.imagesprint.infrastructure.user.strategy

import com.imagesprint.core.domain.user.SocialProvider
import com.imagesprint.core.port.out.user.SocialAuthPort
import com.imagesprint.core.port.out.user.SocialAuthPortResolver
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class SocialAuthAdapterResolver(
    @Qualifier("KAKAO") private val kakaoAdapter: SocialAuthPort,
    @Qualifier("NAVER") private val naverAdapter: SocialAuthPort,
) : SocialAuthPortResolver {

    override fun resolve(provider: SocialProvider): SocialAuthPort {
        return when (provider) {
            SocialProvider.KAKAO -> kakaoAdapter
            SocialProvider.NAVER -> naverAdapter
        }
    }
}
