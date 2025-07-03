package com.imagesprint.apiserver.security

import com.imagesprint.core.port.output.token.TokenProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val tokenProvider: TokenProvider,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = extractToken(request)

        if (token != null && tokenProvider.isValidToken(token)) {
            val userId = tokenProvider.getUserIdFromToken(token)
            val provider = tokenProvider.getProviderFromToken(token)

            val authentication =
                UsernamePasswordAuthenticationToken(
                    AuthenticatedUser(userId, provider),
                    null,
                    emptyList(),
                )
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization") ?: return null
        return if (authHeader.startsWith("Bearer ")) authHeader.removePrefix("Bearer ") else null
    }
}
