package com.imagesprint.infrastructure.job.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface ConversionOptionJpaRepository : JpaRepository<ConversionOptionEntity, Long>
