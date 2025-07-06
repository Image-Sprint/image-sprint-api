package com.imagesprint.apiserver.controller.common

import org.springframework.http.HttpStatus

/**
 * 모든 Controller에서 상속받아 사용할 수 있는 Base 클래스.
 * ApiResultResponse 응답을 간결하게 만들어줌.
 */
abstract class BaseController {
    protected fun <T> ok(data: T): ApiResultResponse<T> = ApiResultResponse.of(HttpStatus.OK, true, data)

    protected fun <T> success(
        message: String,
        data: T?,
    ): ApiResultResponse<T> = ApiResultResponse.of(HttpStatus.OK, true, message, data)

    protected fun fail(message: String): ApiResultResponse<Nothing?> = ApiResultResponse.of(HttpStatus.BAD_REQUEST, false, message, null)

    protected fun fail(
        code: HttpStatus,
        message: String,
    ): ApiResultResponse<Nothing?> = ApiResultResponse.of(code, false, message, null)

    protected fun unauthorized(message: String = "인증이 필요합니다."): ApiResultResponse<Nothing?> =
        ApiResultResponse.of(HttpStatus.UNAUTHORIZED, false, message, null)

    protected fun forbidden(message: String = "접근이 금지되었습니다."): ApiResultResponse<Nothing?> =
        ApiResultResponse.of(HttpStatus.FORBIDDEN, false, message, null)

    protected fun notFound(message: String = "리소스를 찾을 수 없습니다."): ApiResultResponse<Nothing?> =
        ApiResultResponse.of(HttpStatus.NOT_FOUND, false, message, null)

    protected fun internalServerError(message: String = "서버 오류가 발생했습니다."): ApiResultResponse<Nothing?> =
        ApiResultResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, false, message, null)

    protected fun noContent(): ApiResultResponse<Nothing?> = ApiResultResponse.of(HttpStatus.NO_CONTENT, true, "No Content", null)

    protected fun <T> created(data: T): ApiResultResponse<T> = ApiResultResponse.of(HttpStatus.CREATED, true, "Created", data)

    protected fun <T> created(
        message: String,
        data: T,
    ): ApiResultResponse<T> = ApiResultResponse.of(HttpStatus.CREATED, true, message, data)
}
