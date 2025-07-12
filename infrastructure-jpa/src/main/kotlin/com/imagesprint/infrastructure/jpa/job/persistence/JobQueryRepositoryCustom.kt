package com.imagesprint.infrastructure.jpa.job.persistence

interface JobQueryRepositoryCustom {
    fun findByUserIdWithCursor(
        userId: Long,
        cursor: Long?,
        limit: Int,
    ): List<JobEntity>
}
