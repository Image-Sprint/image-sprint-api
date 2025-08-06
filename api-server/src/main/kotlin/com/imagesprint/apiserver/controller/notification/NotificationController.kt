package com.imagesprint.apiserver.controller.notification

import com.imagesprint.apiserver.controller.common.ApiResultResponse
import com.imagesprint.apiserver.controller.common.ApiResultResponse.Companion.ok
import com.imagesprint.apiserver.controller.common.ApiVersions
import com.imagesprint.apiserver.controller.notification.dto.NotificationPageResponse
import com.imagesprint.apiserver.security.AuthenticatedUser
import com.imagesprint.core.port.input.notification.GetNotificationPageQuery
import com.imagesprint.core.port.input.notification.NotificationQueryUseCase
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${ApiVersions.V1}/notifications")
class NotificationController(
    private val notificationQueryUseCase: NotificationQueryUseCase,
) {
    @GetMapping
    fun getMyNotifications(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @RequestParam cursor: Long?,
        @RequestParam(defaultValue = "10") pageSize: Int,
    ): ApiResultResponse<NotificationPageResponse> {
        val query = GetNotificationPageQuery(user.userId, cursor, pageSize)
        val result = notificationQueryUseCase.getNotifications(query)

        return ok(NotificationPageResponse.from(result))
    }
}
