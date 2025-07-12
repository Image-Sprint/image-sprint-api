package com.imagesprint.workerserver.persistence

import com.imagesprint.core.port.input.job.JobStatus
import com.imagesprint.core.port.output.webhook.ReactiveWebhookLogRepository
import com.imagesprint.core.port.output.webhook.ReactiveWebhookRepository
import com.imagesprint.core.port.output.webhook.ReactiveWebhookSender
import org.springframework.stereotype.Component

@Component
class WebhookDispatcher(
    private val webhookRepository: ReactiveWebhookRepository,
    private val sender: ReactiveWebhookSender,
    private val logRepository: ReactiveWebhookLogRepository,
) {
    suspend fun dispatch(
        userId: Long,
        jobId: Long,
        status: JobStatus,
    ) {
        val payload = """{"jobId":$jobId,"status":"$status"}"""
        val webhooks = webhookRepository.getForUser(userId)
        webhooks.forEach { webhook ->
            val log = sender.send(webhook, payload)
            logRepository.save(log)
        }
    }
}
