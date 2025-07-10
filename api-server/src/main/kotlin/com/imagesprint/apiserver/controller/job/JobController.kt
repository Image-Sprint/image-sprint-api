package com.imagesprint.apiserver.controller.job

import com.imagesprint.apiserver.controller.common.ApiResultResponse
import com.imagesprint.apiserver.controller.common.ApiResultResponse.Companion.ok
import com.imagesprint.apiserver.controller.common.ApiVersions
import com.imagesprint.apiserver.controller.job.dto.CreateJobOptionRequest
import com.imagesprint.apiserver.controller.job.dto.JobResponse
import com.imagesprint.apiserver.security.AuthenticatedUser
import com.imagesprint.core.exception.CustomException
import com.imagesprint.core.exception.ErrorCode
import com.imagesprint.core.port.input.job.CreateJobUseCase
import com.imagesprint.core.port.input.job.GetMyJobsUseCase
import com.imagesprint.core.port.input.job.ImageUploadMeta
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("${ApiVersions.V1}/jobs")
class JobController(
    private val createJobUseCase: CreateJobUseCase,
    private val getMyJobsUseCase: GetMyJobsUseCase,
) {
    @GetMapping
    fun getJobs(
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ApiResultResponse<List<JobResponse>> {
        val result = getMyJobsUseCase.getMyJobs(user.userId)
        return ok(result.map { JobResponse.from(it) })
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createJob(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @RequestPart("files") files: List<MultipartFile>,
        @RequestPart("options") @Valid request: CreateJobOptionRequest,
    ): ApiResultResponse<JobResponse> {
        // 파일 개수 제한
        if (files.isEmpty() || files.size > 50) {
            throw CustomException(ErrorCode.INVALID_IMAGE_COUNT)
        }

        val fileMetas =
            files.map {
                ImageUploadMeta(
                    originalFilename = it.originalFilename ?: "unnamed",
                    contentType = it.contentType ?: "application/octet-stream",
                    size = it.size,
                    inputStream = it.inputStream,
                )
            }
        val command = request.toCommand(user.userId, fileMetas)
        val result = createJobUseCase.execute(command)

        return ok(JobResponse.from(result))
    }
}
