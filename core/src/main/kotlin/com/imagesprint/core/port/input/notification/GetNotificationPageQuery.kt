package com.imagesprint.core.port.input.notification

data class GetNotificationPageQuery(
    val userId: Long,
    val cursor: Long?,
    val pageSize: Int,
)
