package com.imagesprint

import com.imagesprint.workerserver.consumer.JobQueueConsumer
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WorkerServerApplication

fun main(args: Array<String>) {
    runBlocking {
        val context = runApplication<WorkerServerApplication>(*args)
        context.getBean(JobQueueConsumer::class.java).consumeLoop()
        // 여기를 막아두면 JVM이 종료되지 않음
        awaitCancellation()
    }
}
