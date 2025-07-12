package com.imagesprint.workerserver.client

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
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
    fun `단위 - presigned 업로드 URL을 정상적으로 생성한다`() {
        // given
        val jobId = 1L
        val userId = 42L
        val mockUrl = URL("https://example.com/presigned-url")

        val mockPutRequest = slot<PutObjectPresignRequest>()
        every { s3Presigner.presignPutObject(capture(mockPutRequest)) } returns
            mockk {
                every { url() } returns mockUrl
            }

        // when
        val url = s3ClientAdapter.generatePresignedUploadUrl(userId, jobId)

        // then
        assertThat(url).isEqualTo(mockUrl.toString())
        val putObject = mockPutRequest.captured.putObjectRequest()
        assertThat(putObject.bucket()).isEqualTo("test-bucket")
        assertThat(putObject.key()).isEqualTo("users/$userId/jobs/$jobId/result.zip")
    }

    @Test
    fun `단위 - presigned 다운로드 URL을 정상적으로 생성한다`() {
        val jobId = 1L
        val userId = 42L
        val mockUrl = URL("https://example.com/presigned-url")

        val mockGetRequest = slot<GetObjectPresignRequest>()
        every { s3Presigner.presignGetObject(capture(mockGetRequest)) } returns
            mockk {
                every { url() } returns mockUrl
            }

        val url = s3ClientAdapter.generatePresignedDownloadUrl(userId, jobId)

        assertThat(url).isEqualTo(mockUrl.toString())
        val getObject = mockGetRequest.captured.getObjectRequest()
        assertThat(getObject.bucket()).isEqualTo("test-bucket")
        assertThat(getObject.key()).isEqualTo("users/$userId/jobs/$jobId/result.zip")
    }

    @Test
    fun `단위 - presigned 업로드 URL 생성 실패 시 예외가 발생한다`() {
        val jobId = 1L
        val userId = 42L

        every {
            s3Presigner.presignPutObject(any<PutObjectPresignRequest>())
        } throws RuntimeException("Presign failed")

        assertThatThrownBy {
            s3ClientAdapter.generatePresignedUploadUrl(userId, jobId)
        }.isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("Presign failed")
    }
}
