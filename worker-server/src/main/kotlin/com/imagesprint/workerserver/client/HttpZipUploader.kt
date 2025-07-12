package com.imagesprint.workerserver.client

import org.apache.logging.log4j.LogManager.getLogger
import org.springframework.stereotype.Component
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
class HttpZipUploader {
    fun upload(
        url: String,
        file: File,
    ) {
        val logger = getLogger(HttpZipUploader::class.java)

        val client = HttpClient.newHttpClient()
        val request =
            HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/zip")
                .PUT(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            logger.error("ZIP 업로드 실패: ${response.statusCode()} - ${response.body()}")
            throw RuntimeException("ZIP 업로드 실패: ${response.statusCode()}")
        }
    }
}
