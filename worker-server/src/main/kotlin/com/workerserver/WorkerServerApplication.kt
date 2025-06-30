package com.workerserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WorkerServerApplication

fun main(args: Array<String>) {
    runApplication<WorkerServerApplication>(*args) {
        // 웹 서버 없이 실행하도록 설정하는 옵션, 워커라서 굳이 웹서버 띄울 필요가 없음
        webApplicationType = org.springframework.boot.WebApplicationType.NONE
    }
}