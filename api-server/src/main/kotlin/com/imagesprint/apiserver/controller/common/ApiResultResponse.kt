package com.imagesprint.apiserver.controller.common

import org.springframework.http.HttpStatus

data class ApiResultResponse<T>(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: T?,
) {
    companion object {
        fun <T> of(
            status: HttpStatus,
            success: Boolean,
            message: String,
            data: T?,
        ): ApiResultResponse<T> = ApiResultResponse(status.value(), success, message, data)

        fun <T> of(
            status: HttpStatus,
            success: Boolean,
            data: T?,
        ): ApiResultResponse<T> = ApiResultResponse(status.value(), success, status.name, data)

        fun <T> ok(data: T?): ApiResultResponse<T> = of(HttpStatus.OK, true, data)

        fun ok(): ApiResultResponse<Unit> = of(HttpStatus.OK, true, "OK", Unit)
    }
}
