package com.imagesprint.infrastructure.token.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface TokenJpaRepository : JpaRepository<TokenEntity, Long>
