package com.imagesprint.core.exception

enum class ErrorCode(
    val code: String,
    val message: String,
) {
    // USER
    USER_NOT_FOUND("USER_404", "유저를 찾을 수 없습니다."),
    INVALID_TOKEN("AUTH_401", "유효하지 않은 토큰입니다."),

    // WEBHOOK
    INVALID_WEBHOOK_URL("WEBHOOK_400", "유효하지 않은 웹훅 URL입니다."),
    WEBHOOK_REQUEST_FAILED("WEBHOOK_422", "웹훅 요청 전송에 실패했습니다."),
    WEBHOOK_NOT_FOUND("WEBHOOK_404", "해당 웹훅을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("COMMON_500", "서버 오류가 발생했습니다."),
}
