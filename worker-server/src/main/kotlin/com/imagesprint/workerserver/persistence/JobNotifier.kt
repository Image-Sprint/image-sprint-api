package com.imagesprint.workerserver.persistence

import com.imagesprint.core.domain.job.Job
import com.imagesprint.core.port.output.notfication.ReactiveNotificationRepository
import org.springframework.stereotype.Component

@Component
class JobNotifier(
    private val notificationRepository: ReactiveNotificationRepository,
) {
    suspend fun notifyStarted(job: Job) {
        notificationRepository.createJobStartedNotification(job.userId, job.jobId!!)
    }

    suspend fun notifyFinished(
        job: Job,
        success: Boolean,
    ) {
        notificationRepository.createJobFinishedNotification(job.userId, job.jobId!!, success)
    }
}
