package com.imagesprint.core.port.output.user

import com.imagesprint.core.domain.user.SocialProvider

interface SocialAuthPortResolver {
    fun resolve(provider: SocialProvider): SocialAuthPort
}
