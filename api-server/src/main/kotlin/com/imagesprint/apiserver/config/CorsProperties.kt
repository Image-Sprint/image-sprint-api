package com.imagesprint.apiserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.cors")
data class CorsProperties(
    var allowedOrigins: String = "",
    var allowedMethods: String = "",
    var allowedHeaders: String = "",
    var maxAge: Long = 3600,
)
