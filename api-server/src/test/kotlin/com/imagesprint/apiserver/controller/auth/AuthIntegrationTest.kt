package com.imagesprint.apiserver.controller.auth

import com.imagesprint.apiserver.support.SocialAuthMockConfig
import com.imagesprint.infrastructure.common.DatabaseCleaner
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.yml"])
@Import(SocialAuthMockConfig::class)
class AuthIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var databaseCleaner: DatabaseCleaner

    @BeforeEach
    fun setup() {
        databaseCleaner.truncate()
    }

    @Test
    fun `로그인 요청 시 accessToken과 쿠키가 반환된다`() {
        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "authorizationCode": "fake-code",
                        "provider": "KAKAO",
                        "state": "test"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty)
            .andExpect(header().exists("Set-Cookie"))
    }
}
