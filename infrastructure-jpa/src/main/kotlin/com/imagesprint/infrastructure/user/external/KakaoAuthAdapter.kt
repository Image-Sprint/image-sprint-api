package com.imagesprint.infrastructure.user.external

import com.fasterxml.jackson.annotation.JsonProperty
import com.imagesprint.core.port.output.user.SocialAuthPort
import com.imagesprint.core.port.output.user.SocialUserInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

@Component("KAKAO")
class KakaoAuthAdapter(
    private val webClient: WebClient,
    @Value("\${oauth.kakao.client-id}") private val clientId: String,
    @Value("\${oauth.kakao.redirect-uri}") private val redirectUri: String,
) : SocialAuthPort {
    private val log: Logger = LoggerFactory.getLogger(KakaoAuthAdapter::class.java)

    override fun getUserInfo(
        authCode: String,
        state: String?,
    ): SocialUserInfo {
        val token = fetchAccessToken(authCode)
        return fetchUserInfo(token)
    }

    private fun fetchAccessToken(code: String): String {
        try {
            val tokenResponse =
                webClient
                    .post()
                    .uri("https://kauth.kakao.com/oauth/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(
                        BodyInserters
                            .fromFormData("grant_type", "authorization_code")
                            .with("client_id", clientId)
                            .with("redirect_uri", redirectUri)
                            .with("code", code),
                    ).retrieve()
                    .bodyToMono(KakaoTokenResponse::class.java)
                    .block() ?: throw RuntimeException("카카오 토큰 응답이 null입니다")

            return tokenResponse.accessToken
        } catch (e: Exception) {
            log.error("카카오 AccessToken 요청 실패: {}", e.message)
            throw RuntimeException("카카오 토큰 발급 실패", e)
        }
    }

    private fun fetchUserInfo(accessToken: String): SocialUserInfo =
        try {
            val response =
                webClient
                    .get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header("Authorization", "Bearer $accessToken")
                    .retrieve()
                    .bodyToMono(KakaoUserResponse::class.java)
                    .block() ?: throw RuntimeException("카카오 유저 정보 응답이 null입니다.")

            SocialUserInfo(
                response.kakaoAccount.email ?: throw RuntimeException("카카오 계정에 이메일이 없습니다."),
                response.kakaoAccount.profile?.nickname ?: "",
            )
        } catch (e: Exception) {
            log.error("카카오 유저 정보 조회 실패: {}", e.message)
            throw RuntimeException("카카오 유저 정보 조회 중 오류가 발생했습니다.", e)
        }

    data class KakaoTokenResponse(
        @JsonProperty("access_token")
        val accessToken: String,
    )

    data class KakaoUserResponse(
        @JsonProperty("kakao_account")
        val kakaoAccount: KakaoAccount,
    ) {
        data class KakaoAccount(
            @JsonProperty("email")
            val email: String?,
            @JsonProperty("profile")
            val profile: Profile?,
        )

        data class Profile(
            @JsonProperty("nickname")
            val nickname: String?,
        )
    }
}
