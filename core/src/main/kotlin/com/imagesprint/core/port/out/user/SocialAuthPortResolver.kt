package com.imagesprint.core.port.out.user

import com.imagesprint.core.domain.user.SocialProvider

interface SocialAuthPortResolver {
    fun resolve(provider: SocialProvider): SocialAuthPort
}
