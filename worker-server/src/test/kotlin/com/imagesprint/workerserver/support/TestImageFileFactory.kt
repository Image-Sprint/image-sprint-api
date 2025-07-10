package com.imagesprint.workerserver.support

import java.io.File

object TestImageFileFactory {
    fun create(
        jobId: Long,
        imageId: Long,
        resourceName: String = "sample.jpg",
    ): File {
        val source = File("src/test/resources/$resourceName")
        require(source.exists()) { "테스트 리소스 파일이 존재하지 않음: ${source.path}" }

        val baseDir = File(System.getProperty("java.io.tmpdir"), jobId.toString())
        baseDir.mkdirs()

        val dest = File(baseDir, "${imageId}_$resourceName")
        source.copyTo(dest, overwrite = true)
        return dest
    }
}
