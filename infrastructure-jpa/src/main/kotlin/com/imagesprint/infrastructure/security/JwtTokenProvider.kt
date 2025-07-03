@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.imagesprint.infrastructure.security

import com.imagesprint.core.port.output.token.TokenProvider
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtTokenProvider(
    @Value("\${jwt.token-secret}") private val secret: String,
    @Value("\${jwt.access-token-expiry}") private val accessTokenExpiry: Long,
    @Value("\${jwt.refresh-token-expiry}") private val refreshTokenExpiry: Long,
) : TokenProvider {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    override fun generateAccessToken(
        userId: Long,
        provider: String,
    ): String {
        val now = Date()
        val expiry = Date(now.time + accessTokenExpiry)

        return Jwts
            .builder()
            .setSubject(userId.toString())
            .claim("provider", provider)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key)
            .compact()
    }

    override fun generateRefreshToken(
        userId: Long,
        provider: String,
    ): String {
        val now = Date()
        val expiry = Date(now.time + refreshTokenExpiry)

        return Jwts
            .builder()
            .setSubject(userId.toString())
            .claim("provider", provider)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key)
            .compact()
    }

    override fun isValidToken(token: String): Boolean =
        try {
            Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }

    override fun getUserIdFromToken(token: String): Long {
        val claims =
            Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
        return claims.subject.toLong()
    }

    override fun getProviderFromToken(token: String): String {
        val claims =
            Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
        return claims["provider"].toString()
    }
}
