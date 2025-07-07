package com.imagesprint.core.port.output.job

import com.imagesprint.core.domain.job.ConversionOption

interface ConversionOptionRepository {
    fun saveOption(conversionOption: ConversionOption): ConversionOption
}
