package com.imagesprint.infrastructure.jpa.token.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface TokenJpaRepository : JpaRepository<TokenEntity, Long> {
    fun getByUserId(userId: Long): TokenEntity?
}
