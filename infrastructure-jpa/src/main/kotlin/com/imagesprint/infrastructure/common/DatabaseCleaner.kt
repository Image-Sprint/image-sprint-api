package com.imagesprint.infrastructure.common

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DatabaseCleaner {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    fun truncate() {
        entityManager.flush()
        entityManager.clear()

        // 외래 키 무시
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate()

        // 테이블 목록 조회 (MySQL 기준)
        val tableNames = entityManager.createNativeQuery(
            "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()"
        ).resultList

        tableNames.forEach { tableName ->
            entityManager.createNativeQuery("TRUNCATE TABLE `$tableName`").executeUpdate()
        }

        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate()
    }
}
