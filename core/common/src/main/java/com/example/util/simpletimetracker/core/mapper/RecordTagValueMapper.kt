package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.common.R
import com.example.util.simpletimetracker.core.repo.BaseResourceRepo
import com.example.util.simpletimetracker.domain.recordTag.RecordTagValueFormatMapper
import javax.inject.Inject

class RecordTagValueMapper @Inject constructor(
    private val resourceRepo: BaseResourceRepo,
    private val recordTagValueFormatMapper: RecordTagValueFormatMapper,
) {

    fun mapTagValue(
        value: Double,
        valueSuffix: String,
    ): String {
        val actualValue = recordTagValueFormatMapper.map(value)
        return if (valueSuffix.isEmpty()) {
            actualValue
        } else {
            resourceRepo.getString(R.string.separator_template, actualValue, valueSuffix)
        }
    }

    fun getNameWithValue(
        name: String,
        value: Double,
        valueSuffix: String,
    ): String {
        val tagValue = mapTagValue(value, valueSuffix)
        return resourceRepo.getString(R.string.separator_template, name, "($tagValue)")
    }
}