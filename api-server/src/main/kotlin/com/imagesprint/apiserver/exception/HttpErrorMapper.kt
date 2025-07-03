package com.imagesprint.apiserver.exception

import com.imagesprint.core.exception.ErrorCode
import org.springframework.http.HttpStatus

fun ErrorCode.toHttpStatus(): HttpStatus = when (this) {
    ErrorCode.USER_NOT_FOUND -> HttpStatus.NOT_FOUND
    ErrorCode.INVALID_TOKEN -> HttpStatus.UNAUTHORIZED
    else -> HttpStatus.INTERNAL_SERVER_ERROR
}
