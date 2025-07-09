package com.imagesprint.workerserver.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.net.URL
import java.time.Duration

@Component
class S3ClientAdapter(
    private val s3Presigner: S3Presigner,
    @Value("\${aws.s3.bucket}") private val bucket: String,
) {
    // Presigned GET URL 발급 (다운로드용)
    fun generatePresignedUrl(
        userId: Long,
        jobId: Long,
    ): String {
        val key = "users/$userId/jobs/$jobId/result.zip"

        val getObjectRequest =
            GetObjectRequest
                .builder()
                .bucket(bucket)
                .key(key)
                .build()

        val presignRequest =
            GetObjectPresignRequest
                .builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofDays(1))
                .build()

        val presignedUrl: URL = s3Presigner.presignGetObject(presignRequest).url()
        return presignedUrl.toString()
    }
}
