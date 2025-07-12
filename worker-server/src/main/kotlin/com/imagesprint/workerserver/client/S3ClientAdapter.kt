package com.imagesprint.workerserver.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Component
class S3ClientAdapter(
    private val s3Presigner: S3Presigner,
    @Value("\${aws.s3.bucket}") private val bucket: String,
) {
    private fun buildObjectKey(
        userId: Long,
        jobId: Long,
    ): String = "users/$userId/jobs/$jobId/result.zip"

    /**
     * 업로드용 Presigned PUT URL
     */
    fun generatePresignedUploadUrl(
        userId: Long,
        jobId: Long,
        validDuration: Duration = Duration.ofMinutes(5), // 업로드는 짧게(5분) 설정
    ): String {
        val key = buildObjectKey(userId, jobId)

        val putObjectRequest =
            PutObjectRequest
                .builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/zip")
                .build()

        val presignRequest =
            PutObjectPresignRequest
                .builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(validDuration)
                .build()

        return s3Presigner.presignPutObject(presignRequest).url().toString()
    }

    /**
     * 다운로드용 Presigned GET URL
     */
    fun generatePresignedDownloadUrl(
        userId: Long,
        jobId: Long,
        validDuration: Duration = Duration.ofDays(1), // 다운로드는 길게(1일) 설정
    ): String {
        val key = buildObjectKey(userId, jobId)

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
                .signatureDuration(validDuration)
                .build()

        return s3Presigner.presignGetObject(presignRequest).url().toString()
    }
}
