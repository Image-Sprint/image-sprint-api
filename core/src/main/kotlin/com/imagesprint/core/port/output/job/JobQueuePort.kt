package com.imagesprint.core.port.output.job

interface JobQueuePort {
    fun enqueueJob(jobId: Long)
}
