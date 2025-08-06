package com.imagesprint.infrastructure.jpa.notification.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface NotificationJpaRepository :
    JpaRepository<NotificationEntity, Long>,
    NotificationQueryRepositoryCustom
