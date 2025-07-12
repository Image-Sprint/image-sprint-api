package com.imagesprint.core.port.input.job

import kotlinx.coroutines.flow.Flow

interface SubscribeJobProgressUseCase {
    fun subscribeAll(): Flow<JobProgressResult>
}
