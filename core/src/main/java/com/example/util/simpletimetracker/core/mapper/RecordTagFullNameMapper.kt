package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import javax.inject.Inject

class RecordTagFullNameMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun getNameWithValue(name: String, value: Double): String {
        // TODO TAG do better?
        val actualValue = value.toBigDecimal().stripTrailingZeros().toPlainString()
        return resourceRepo.getString(
            R.string.separator_template,
            name,
            "($actualValue)",
        )
    }

    fun getFullName(
        tags: List<RecordTag>,
        tagData: List<RecordBase.Tag>,
    ): String {
        val tagDataMap = tagData.associateBy { it.tagId }
        return tags.joinToString(
            separator = ", ",
            transform = { tag ->
                tagDataMap[tag.id]?.numericValue
                    ?.let { value -> getNameWithValue(tag.name, value) }
                    ?: tag.name
            },
        )
    }
}