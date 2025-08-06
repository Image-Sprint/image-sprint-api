package com.imagesprint.apiserver.support

import com.imagesprint.apiserver.security.AuthenticatedUser
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockAuthenticatedUserSecurityContextFactory::class)
annotation class WithMockAuthenticatedUser(
    val userId: Long = 1L,
    val provider: String = "KAKAO",
)

class WithMockAuthenticatedUserSecurityContextFactory : WithSecurityContextFactory<WithMockAuthenticatedUser> {
    override fun createSecurityContext(annotation: WithMockAuthenticatedUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()
        val principal = AuthenticatedUser(annotation.userId, annotation.provider)
        context.authentication = UsernamePasswordAuthenticationToken(principal, null, listOf())
        return context
    }
}
