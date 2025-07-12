package com.imagesprint.core.port.input.job

data class GetJobPageQuery(
    val userId: Long,
    val cursor: Long?,
    val pageSize: Int,
)
