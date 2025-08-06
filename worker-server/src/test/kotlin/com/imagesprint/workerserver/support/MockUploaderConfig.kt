package com.imagesprint.workerserver.support

import com.imagesprint.workerserver.client.HttpZipUploader
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class MockUploaderConfig {
    @Bean
    @Primary
    fun mockUploader(): HttpZipUploader =
        mockk {
            coEvery { upload(any(), any()) } just Runs
        }
}
