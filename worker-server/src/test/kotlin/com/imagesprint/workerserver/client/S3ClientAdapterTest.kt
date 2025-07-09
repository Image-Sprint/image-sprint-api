package com.imagesprint.workerserver.client

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.net.URL
import kotlin.test.Test

class S3ClientAdapterTest {
    private lateinit var s3Presigner: S3Presigner
    private lateinit var s3ClientAdapter: S3ClientAdapter

    @BeforeEach
    fun setUp() {
        s3Presigner = mockk()
        s3ClientAdapter = S3ClientAdapter(s3Presigner, "test-bucket")
    }

    @Test
    fun `단위 - presigned URL을 정상적으로 생성한다`() {
        // given
        val jobId = 1L
        val userId = 42L
        val mockUrl = URL("https://example.com/presigned-url")

        val mockRequest = slot<GetObjectPresignRequest>()
        every { s3Presigner.presignGetObject(capture(mockRequest)) } returns
            mockk {
                every { url() } returns mockUrl
            }

        // when
        val url = s3ClientAdapter.generatePresignedUrl(userId, jobId)

        // then
        assertThat(url).isEqualTo(mockUrl.toString())
        assertThat(mockRequest.captured.getObjectRequest().bucket()).isEqualTo("test-bucket")
        assertThat(mockRequest.captured.getObjectRequest().key()).contains("users/$userId/jobs/$jobId")
    }

    @Test
    fun `단위 - presigned URL 생성 실패 시 예외가 발생한다`() {
        // given
        val jobId = 1L
        val userId = 42L

        every {
            s3Presigner.presignGetObject(any<GetObjectPresignRequest>())
        } throws RuntimeException("Presign failed")

        // when & then
        assertThatThrownBy {
            s3ClientAdapter.generatePresignedUrl(userId, jobId)
        }.isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("Presign failed")
    }
}
