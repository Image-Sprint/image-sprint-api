package com.imagesprint.infrastructure.jpa.job.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface ConversionOptionJpaRepository : JpaRepository<ConversionOptionEntity, Long>
