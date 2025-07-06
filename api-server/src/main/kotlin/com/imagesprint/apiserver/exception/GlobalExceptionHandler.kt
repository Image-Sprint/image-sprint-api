package com.imagesprint.apiserver.exception

import com.imagesprint.apiserver.controller.common.ApiResultResponse
import com.imagesprint.apiserver.controller.common.BaseController
import com.imagesprint.core.exception.CustomException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@ResponseStatus(HttpStatus.OK)
class GlobalExceptionHandler : BaseController() {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @Value("\${spring.profiles.active:}")
    private lateinit var activeProfile: String

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ApiResultResponse<Nothing?> {
        val msg =
            e.bindingResult.allErrors
                .firstOrNull()
                ?.defaultMessage ?: "잘못된 요청입니다."
        log.warn("Validation 실패: {}", msg)

        return fail(msg)
    }

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ApiResultResponse<Nothing?> {
        val errorCode = e.errorCode
        val status = errorCode.toHttpStatus()
        log.error("예외 발생: {}", errorCode.message)

        return fail(status, errorCode.message)
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleGeneric(e: Exception): ApiResultResponse<Nothing?> {
        if (activeProfile == "local" || activeProfile == "dev") {
            log.error("예상치 못한 예외 발생", e) // stack trace 포함
        } else {
            log.error("예상치 못한 예외 발생: {}", e.message) // prod는 요약만
        }

        return fail(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 서버 에러가 발생했습니다.")
    }
}
