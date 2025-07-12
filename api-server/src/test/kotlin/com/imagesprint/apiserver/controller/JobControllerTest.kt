package com.imagesprint.apiserver.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.imagesprint.apiserver.consumer.JobProgressStreamConsumer
import com.imagesprint.apiserver.controller.job.JobController
import com.imagesprint.apiserver.controller.job.dto.CreateJobOptionRequest
import com.imagesprint.apiserver.security.AuthenticatedUser
import com.imagesprint.core.domain.job.Job
import com.imagesprint.core.exception.CustomException
import com.imagesprint.core.exception.ErrorCode
import com.imagesprint.core.port.input.job.*
import com.imagesprint.core.port.output.token.TokenProvider
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.LocalDateTime
import kotlin.test.Test

@WebMvcTest(JobController::class)
@AutoConfigureMockMvc(addFilters = false)
class JobControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var createJobUseCase: CreateJobUseCase

    @MockkBean
    private lateinit var getMyJobsUseCase: GetMyJobsUseCase

    @MockkBean
    private lateinit var jobProgressStreamConsumer: JobProgressStreamConsumer

    @MockkBean
    private lateinit var tokenProvider: TokenProvider

    private val userId = 1L

    @BeforeEach
    fun setup() {
        val user = AuthenticatedUser(userId = userId, provider = "KAKAO")
        val authentication = UsernamePasswordAuthenticationToken(user, null)
        SecurityContextHolder.getContext().authentication = authentication
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Nested
    inner class GetMyJobsTest {
        @Test
        fun `컨트롤러 - 정상적으로 사용자의 Job 목록을 조회한다`() {
            // given
            val jobList =
                JobPage(
                    jobs =
                        listOf(
                            Job(
                                jobId = 101L,
                                jobName = "Job-101",
                                userId = userId,
                                status = JobStatus.DONE,
                                imageCount = 2,
                                createdAt = LocalDateTime.now(),
                                originalSize = 123456,
                            ),
                            Job(
                                jobId = 102L,
                                jobName = "Job-102",
                                userId = userId,
                                status = JobStatus.DONE,
                                imageCount = 1,
                                createdAt = LocalDateTime.now(),
                                originalSize = 654321,
                            ),
                        ),
                    hasNext = false,
                    nextCursor = null,
                )

            val query =
                GetJobPageQuery(
                    userId = userId,
                    cursor = null,
                    pageSize = 10,
                )

            every { getMyJobsUseCase.getMyJobs(query) } returns jobList

            // when & then
            mockMvc.get("/api/v1/jobs").andExpect {
                status { isOk() }
                jsonPath("$.data.jobs.size()") { value(2) }
                jsonPath("$.data.jobs[0].jobId") { value(101L) }
            }

            verify(exactly = 1) { getMyJobsUseCase.getMyJobs(query) }
        }

        @Test
        fun `컨트롤러 - 조회 결과가 없으면 빈 리스트를 반환한다`() {
            // given
            val query =
                GetJobPageQuery(
                    userId = userId,
                    cursor = null,
                    pageSize = 10,
                )
            val jobList =
                JobPage(
                    jobs =
                        emptyList(),
                    hasNext = false,
                    nextCursor = null,
                )
            every { getMyJobsUseCase.getMyJobs(query) } returns jobList

            mockMvc.get("/api/v1/jobs").andExpect {
                status { isOk() }
                jsonPath("$.data.jobs") { isArray() }
                jsonPath("$.data.jobs.size()") { value(0) }
            }

            verify(exactly = 1) { getMyJobsUseCase.getMyJobs(query) }
        }
    }

    @Nested
    inner class StreamProgressTest {
        @Test
        fun `컨트롤러 - SSE 스트림 구독 요청시 SseEmitter를 반환한다`() {
            // given
            val emitter = SseEmitter(60_000L)
            every { jobProgressStreamConsumer.subscribe(any()) } answers {
                arg<SseEmitter>(0) // emitter 그대로 반환
            }

            // when & then
            mockMvc
                .get("/api/v1/jobs/progress/stream") {
                    header("Accept", "text/event-stream")
                }.andExpect {
                    status { isOk() }
                    // Content-Type은 실제 전송이 시작되기 전에는 설정되지 않기 때문에 생략
                }

            verify(exactly = 1) { jobProgressStreamConsumer.subscribe(any()) }
        }
    }

    @Nested
    inner class CreateJobTest {
        @Test
        fun `컨트롤러 - 정상적으로 Job을 생성하고 200 OK를 반환한다`() {
            // given
            val file = MockMultipartFile("files", "sample.jpg", "image/jpeg", "image-bytes".toByteArray())
            val request =
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
            val jobResult =
                JobResult(
                    jobId = 10L,
                    status = "PENDING",
                    imageCount = 1,
                    createdAt = LocalDateTime.now(),
                    originalSize = 1024,
                )

            every { createJobUseCase.execute(any()) } returns jobResult

            val optionsPart =
                MockMultipartFile(
                    "options",
                    "options.json",
                    "application/json",
                    objectMapper.writeValueAsBytes(request),
                )

            // when & then
            mockMvc
                .multipart("/api/v1/jobs") {
                    file(file)
                    file(optionsPart)
                    contentType = MediaType.MULTIPART_FORM_DATA
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.data.jobId") { value(10L) }
                    jsonPath("$.data.status") { value("PENDING") }
                }

            verify(exactly = 1) { createJobUseCase.execute(any()) }
        }

        @Test
        fun `컨트롤러 - 파일이 없거나 너무 많으면 status 400 에러를 반환한다`() {
            // given
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
                    MockMultipartFile("files", "file$it.jpg", "image/jpeg", "image-data".toByteArray())
                }

            // when & then
            mockMvc
                .multipart("/api/v1/jobs") {
                    file(optionsPart) // JSON part
                    tooManyFiles.forEach { file(it) } // 이미지 파일들
                    contentType = MediaType.MULTIPART_FORM_DATA
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.status") { value(400) }
                    jsonPath("$.message") { value("1~100개의 이미지를 업로드해야 합니다.") }
                }
        }

        @Test
        fun `컨트롤러 - CreateJobUseCase에서 예외 발생 시 에러코드 반환한`() {
            // given
            val file = MockMultipartFile("files", "sample.jpg", "image/jpeg", "image".toByteArray())
            val request =
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
                    objectMapper.writeValueAsBytes(request),
                )

            every { createJobUseCase.execute(any()) } throws CustomException(ErrorCode.INVALID_RESIZE_WIDTH)

            // when & then
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

            verify(exactly = 1) { createJobUseCase.execute(any()) }
        }
    }
}
