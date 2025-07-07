package com.imagesprint.core.port.input.job

interface CreateJobUseCase {
    fun execute(command: CreateJobCommand): JobResult
}
