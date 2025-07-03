package com.imagesprint.apiserver.controller.common

import org.springframework.http.HttpStatus

data class ApiResultResponse<T>(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: T?
) {
    companion object {
        fun <T> of(status: HttpStatus, success: Boolean, message: String, data: T?): ApiResultResponse<T> {
            return ApiResultResponse(status.value(), success, message, data)
        }

        fun <T> of(status: HttpStatus, success: Boolean, data: T?): ApiResultResponse<T> {
            return ApiResultResponse(status.value(), success, status.name, data)
        }

        fun <T> ok(data: T?): ApiResultResponse<T> {
            return of(HttpStatus.OK, true, data)
        }

        fun ok(): ApiResultResponse<Unit> {
            return of(HttpStatus.OK, true, "OK", Unit)
        }
    }
}
