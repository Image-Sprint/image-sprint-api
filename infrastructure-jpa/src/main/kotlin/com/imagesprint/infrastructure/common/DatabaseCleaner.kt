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

        val tableNames =
            entityManager
                .createNativeQuery(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = 'PUBLIC'",
                ).resultList as List<String>

        tableNames.forEach { tableName ->
            entityManager.createNativeQuery("DELETE FROM \"$tableName\"").executeUpdate()
        }
    }
}
