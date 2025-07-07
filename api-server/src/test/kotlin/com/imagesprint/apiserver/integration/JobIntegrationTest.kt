package com.imagesprint.apiserver.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.imagesprint.apiserver.controller.job.dto.CreateJobOptionRequest
import com.imagesprint.apiserver.support.EmbeddedRedisConfig
import com.imagesprint.apiserver.support.WithMockAuthenticatedUser
import com.imagesprint.core.port.input.job.WatermarkPosition
import com.imagesprint.infrastructure.common.DatabaseCleaner
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.multipart
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.yml"])
@Import(EmbeddedRedisConfig::class)
@ActiveProfiles("test")
class JobIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var databaseCleaner: DatabaseCleaner

    @BeforeEach
    fun setup() {
        databaseCleaner.truncate()
    }

    @Test
    @WithMockAuthenticatedUser(userId = 1, provider = "KAKAO")
    fun `통합 - 정상적으로 Job을 생성하면 200 OK와 결과를 반환한다`() {
        // given
        val file = MockMultipartFile("files", "sample.jpg", "image/jpeg", "image-data".toByteArray())
        val options =
            CreateJobOptionRequest(
                resizeWidth = 800,
                resizeHeight = 600,
                keepRatio = true,
                toFormat = "jpeg",
                quality = 80,
                watermarkText = "sample",
                watermarkPosition = WatermarkPosition.BOTTOM_RIGHT,
                watermarkOpacity = 0.5f,
            )
        val optionsPart =
            MockMultipartFile(
                "options",
                "options.json",
                "application/json",
                objectMapper.writeValueAsBytes(options),
            )

        // when & then
        mockMvc
            .multipart("/api/v1/jobs") {
                file(file)
                file(optionsPart)
                contentType = MediaType.MULTIPART_FORM_DATA
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.jobId") { exists() }
                jsonPath("$.data.status") { value("PENDING") }
            }
    }

    @Test
    @WithMockAuthenticatedUser(userId = 1, provider = "KAKAO")
    fun `통합 - 파일 개수가 0개이거나 100개 초과인 경우 status 400 코드를 반환한다`() {
        val options =
            CreateJobOptionRequest(
                resizeWidth = 800,
                resizeHeight = 600,
                keepRatio = true,
                toFormat = "jpeg",
                quality = 80,
                watermarkText = null,
                watermarkPosition = null,
                watermarkOpacity = null,
            )
        val optionsPart =
            MockMultipartFile(
                "options",
                "options.json",
                "application/json",
                objectMapper.writeValueAsBytes(options),
            )

        val tooManyFiles =
            (1..101).map {
                MockMultipartFile("files", "file$it.jpg", "image/jpeg", "data".toByteArray())
            }

        mockMvc
            .multipart("/api/v1/jobs") {
                file(optionsPart)
                tooManyFiles.forEach { file(it) }
                contentType = MediaType.MULTIPART_FORM_DATA
            }.andExpect {
                status { isOk() }
                jsonPath("$.status") { value(400) }
                jsonPath("$.message") { value("1~100개의 이미지를 업로드해야 합니다.") }
            }
    }

    @Test
    @WithMockAuthenticatedUser(userId = 1, provider = "KAKAO")
    fun `통합 - 유효하지 않은 옵션 값이면 status 400 코드를 반환한다`() {
        val file = MockMultipartFile("files", "sample.jpg", "image/jpeg", "image-data".toByteArray())
        val invalidOptions =
            CreateJobOptionRequest(
                resizeWidth = 0, // invalid
                resizeHeight = 600,
                keepRatio = true,
                toFormat = "jpeg",
                quality = 80,
                watermarkText = null,
                watermarkPosition = null,
                watermarkOpacity = null,
            )
        val optionsPart =
            MockMultipartFile(
                "options",
                "options.json",
                "application/json",
                objectMapper.writeValueAsBytes(invalidOptions),
            )

        mockMvc
            .multipart("/api/v1/jobs") {
                file(file)
                file(optionsPart)
                contentType = MediaType.MULTIPART_FORM_DATA
            }.andExpect {
                status { isOk() }
                jsonPath("$.status") { value(400) }
                jsonPath("$.message") { value("resizeWidth는 양수여야 합니다.") }
            }
    }
}
