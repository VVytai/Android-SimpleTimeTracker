package com.example.util.simpletimetracker.data_local.complexRule

import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.RecordTagValueFormatMapper
import javax.inject.Inject

class ComplexRuleTagValuesMapper @Inject constructor(
    private val recordTagValueFormatMapper: RecordTagValueFormatMapper,
) {

    fun serialize(data: List<RecordBase.Tag>): String {
        return data.mapNotNull { tag ->
            val value = tag.numericValue ?: return@mapNotNull null
            "${tag.tagId}:${recordTagValueFormatMapper.map(value)}"
        }.joinToString(separator = ",")
    }

    fun parse(data: String?): List<RecordBase.Tag> {
        if (data == null || data.isBlank()) return emptyList()

        return data.split(',').mapNotNull { entry ->
            val parts = entry.split(':', limit = 2)
            if (parts.size != 2) return@mapNotNull null
            RecordBase.Tag(
                tagId = parts[0].toLongOrNull() ?: return@mapNotNull null,
                numericValue = parts[1].toDoubleOrNull() ?: return@mapNotNull null,
            )
        }
    }
}
