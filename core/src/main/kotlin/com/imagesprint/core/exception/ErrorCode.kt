package com.imagesprint.core.exception

enum class ErrorCode(
    val code: String,
    val message: String
) {
    USER_NOT_FOUND("USER_404", "사용자를 찾을 수 없습니다."),
    INVALID_TOKEN("AUTH_401", "유효하지 않은 토큰입니다."),
    INTERNAL_SERVER_ERROR("COMMON_500", "서버 오류가 발생했습니다.")
}
