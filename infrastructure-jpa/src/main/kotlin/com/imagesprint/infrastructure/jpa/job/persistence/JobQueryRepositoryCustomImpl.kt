package com.imagesprint.infrastructure.jpa.job.persistence

import com.querydsl.jpa.impl.JPAQueryFactory

class JobQueryRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
) : JobQueryRepositoryCustom {
    override fun findByUserIdWithCursor(
        userId: Long,
        cursor: Long?,
        limit: Int,
    ): List<JobEntity> {
        val job = QJobEntity.jobEntity

        return queryFactory
            .selectFrom(job)
            .where(
                job.userId.eq(userId),
                cursor?.let { job.jobId.lt(it) }, // 커서 조건
            ).orderBy(job.jobId.desc())
            .limit((limit + 1).toLong()) // hasNext 판단 위해 +1
            .fetch()
    }
}
