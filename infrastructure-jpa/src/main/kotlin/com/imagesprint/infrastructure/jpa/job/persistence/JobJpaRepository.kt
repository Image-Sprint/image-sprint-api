package com.imagesprint.infrastructure.jpa.job.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface JobJpaRepository :
    JpaRepository<JobEntity, Long>,
    JobQueryRepositoryCustom {
    fun findAllByUserId(userId: Long): List<JobEntity>
}
