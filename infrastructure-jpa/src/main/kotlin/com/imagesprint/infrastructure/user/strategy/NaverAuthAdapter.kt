package com.imagesprint.infrastructure.user.strategy

import com.fasterxml.jackson.annotation.JsonProperty
import com.imagesprint.core.port.output.user.SocialAuthPort
import com.imagesprint.core.port.output.user.SocialUserInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

@Component("NAVER")
class NaverAuthAdapter(
    private val webClient: WebClient,
    @Value("\${oauth.naver.client-id}") private val clientId: String,
    @Value("\${oauth.naver.client-secret}") private val clientSecret: String,
    @Value("\${oauth.naver.redirect-uri}") private val redirectUri: String,
) : SocialAuthPort {
    private val log: Logger = LoggerFactory.getLogger(NaverAuthAdapter::class.java)

    override fun getUserInfo(
        authCode: String,
        state: String?,
    ): SocialUserInfo {
        val token = fetchAccessToken(authCode, state)
        return fetchUserInfo(token)
    }

    private fun fetchAccessToken(
        code: String,
        state: String?,
    ): String =
        try {
            val tokenResponse =
                webClient
                    .post()
                    .uri("https://nid.naver.com/oauth2.0/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(
                        BodyInserters
                            .fromFormData("grant_type", "authorization_code")
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("redirect_uri", redirectUri)
                            .with("code", code)
                            .with("state", state ?: ""),
                    ).retrieve()
                    .bodyToMono(NaverTokenResponse::class.java)
                    .block() ?: throw RuntimeException("네이버 토큰 응답이 null입니다.")

            tokenResponse.accessToken
        } catch (e: Exception) {
            log.error("네이버 AccessToken 요청 실패: {}", e.message)
            throw RuntimeException("네이버 토큰 발급 실패", e)
        }

    private fun fetchUserInfo(accessToken: String): SocialUserInfo =
        try {
            val response =
                webClient
                    .get()
                    .uri("https://openapi.naver.com/v1/nid/me")
                    .header("Authorization", "Bearer $accessToken")
                    .retrieve()
                    .bodyToMono(NaverUserResponse::class.java)
                    .block() ?: throw RuntimeException("네이버 유저 정보 응답이 null입니다.")

            SocialUserInfo(
                response.response.email ?: throw RuntimeException("이메일을 찾을 수 없습니다."),
                response.response.nickname ?: "",
            )
        } catch (e: Exception) {
            log.error("네이버 유저 정보 조회 실패: {}", e.message)
            throw RuntimeException("네이버 유저 정보 조회 중 오류가 발생했습니다.", e)
        }

    data class NaverTokenResponse(
        @JsonProperty("access_token")
        val accessToken: String,
    )

    data class NaverUserResponse(
        val response: Response,
    ) {
        data class Response(
            val email: String?,
            val nickname: String?,
        )
    }
}
