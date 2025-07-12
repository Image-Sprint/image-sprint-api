package com.imagesprint.infrastructure.jpa.job.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ImageFileJpaRepository : JpaRepository<ImageFileEntity, Long> {
    @Modifying
    @Query("UPDATE ImageFileEntity i SET i.job.jobId = :jobId, i.convertStatus = 'WAITING' WHERE i.imageFileId IN :imageIds")
    fun updateJobIdAndStatus(
        @Param("jobId") jobId: Long,
        @Param("imageIds") imageIds: List<Long>,
    )
}
