package com.imagesprint.core.exception

open class CustomException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)
