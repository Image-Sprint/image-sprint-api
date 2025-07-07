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

    // JOB
    INVALID_QUALITY("JOB_400", "quality는 1~100 사이여야 합니다."),
    INVALID_RESIZE_WIDTH("JOB_400", "resizeWidth는 양수여야 합니다."),
    INVALID_RESIZE_HEIGHT("JOB_400", "resizeHeight는 양수여야 합니다."),
    INVALID_IMAGE_COUNT("JOB_400", "1~100개의 이미지를 업로드해야 합니다."),
    FILE_STORAGE_FAILED("JOB_500", "이미지 파일 저장 중 오류가 발생했습니다."),
    QUEUE_ENQUEUE_FAILED("JOB_500", "작업 큐 등록 중 오류가 발생했습니다."),

    INTERNAL_SERVER_ERROR("COMMON_500", "서버 오류가 발생했습니다."),
}
