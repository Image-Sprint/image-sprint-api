package com.imagesprint.apiserver.exception

import com.imagesprint.core.exception.ErrorCode
import org.springframework.http.HttpStatus

fun ErrorCode.toHttpStatus(): HttpStatus =
    when (this) {
        ErrorCode.USER_NOT_FOUND -> HttpStatus.NOT_FOUND
        ErrorCode.INVALID_TOKEN -> HttpStatus.UNAUTHORIZED
        ErrorCode.INVALID_WEBHOOK_URL -> HttpStatus.BAD_REQUEST
        ErrorCode.WEBHOOK_REQUEST_FAILED -> HttpStatus.UNPROCESSABLE_ENTITY
        ErrorCode.INVALID_QUALITY -> HttpStatus.BAD_REQUEST
        ErrorCode.INVALID_RESIZE_WIDTH -> HttpStatus.BAD_REQUEST
        ErrorCode.INVALID_RESIZE_HEIGHT -> HttpStatus.BAD_REQUEST
        ErrorCode.INVALID_IMAGE_COUNT -> HttpStatus.BAD_REQUEST
        else -> HttpStatus.INTERNAL_SERVER_ERROR
    }
