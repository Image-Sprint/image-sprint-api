package com.imagesprint.infrastructure.job.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface JobJpaRepository : JpaRepository<JobEntity, Long>
