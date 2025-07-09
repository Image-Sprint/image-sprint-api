package com.imagesprint.workerserver.client

import java.io.File

interface ZipUploader {
    fun upload(
        url: String,
        file: File,
    )
}
