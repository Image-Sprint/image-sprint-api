package com.imagesprint.infrastructure.jpa.job.persistence

import com.imagesprint.core.domain.job.ConversionOption
import com.imagesprint.core.domain.job.ImageFile
import com.imagesprint.core.domain.job.Job
import com.imagesprint.core.port.input.job.JobPage
import com.imagesprint.core.port.output.job.ConversionOptionRepository
import com.imagesprint.core.port.output.job.ImageRepository
import com.imagesprint.core.port.output.job.JobRepository
import org.springframework.stereotype.Repository

@Repository
class JobRepositoryImpl(
    private val jobJpaRepository: JobJpaRepository,
    private val imageFileJpaRepository: ImageFileJpaRepository,
    private val conversionOptionJpaRepository: ConversionOptionJpaRepository,
) : JobRepository,
    ImageRepository,
    ConversionOptionRepository {
    override fun saveJob(job: Job): Job {
        val jobEntity = JobEntity.from(job)
        val savedEntity = jobJpaRepository.save(jobEntity)
        return savedEntity.toDomain()
    }

    override fun getMyJobs(userId: Long): List<Job> =
        jobJpaRepository
            .findAllByUserId(userId)
            .map { it.toDomain() }
            .sortedByDescending { it.createdAt }

    override fun getMyJobsByCursor(
        userId: Long,
        cursor: Long?,
        pageSize: Int,
    ): JobPage {
        val results = jobJpaRepository.findByUserIdWithCursor(userId, cursor, pageSize)

        val hasNext = results.size > pageSize
        val pageItems = if (hasNext) results.dropLast(1) else results
        val nextCursor = if (hasNext) pageItems.lastOrNull()?.jobId else null

        return JobPage(
            jobs = pageItems.map { it.toDomain() },
            nextCursor = nextCursor,
            hasNext = hasNext,
        )
    }

    override fun saveImages(images: List<ImageFile>): List<ImageFile> {
        val imageFileEntities = images.map { ImageFileEntity.from(it) }
        val savedEntities = imageFileJpaRepository.saveAll(imageFileEntities)
        return savedEntities.map { it.toDomain() }
    }

    override fun updateJobIdAndStatus(
        jobId: Long,
        imageIds: List<Long>,
    ) {
        imageFileJpaRepository.updateJobIdAndStatus(jobId, imageIds)
    }

    override fun saveOption(conversionOption: ConversionOption): ConversionOption {
        val jobEntity =
            jobJpaRepository
                .getReferenceById(conversionOption.jobId)

        val conversionOptionEntity = ConversionOptionEntity.from(conversionOption, jobEntity)
        val savedEntity = conversionOptionJpaRepository.save(conversionOptionEntity)

        return savedEntity.toDomain()
    }
}
