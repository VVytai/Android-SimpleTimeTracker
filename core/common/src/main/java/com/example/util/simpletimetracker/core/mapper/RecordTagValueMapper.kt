package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.common.R
import com.example.util.simpletimetracker.core.repo.BaseResourceRepo
import javax.inject.Inject

class RecordTagValueMapper @Inject constructor(
    private val resourceRepo: BaseResourceRepo,
) {

    fun map(value: Double): String {
        // TODO TAG do better?
        return value.toBigDecimal().stripTrailingZeros().toPlainString()
    }

    fun getNameWithValue(name: String, value: Double): String {
        val actualValue = map(value)
        return resourceRepo.getString(
            R.string.separator_template,
            name,
            "($actualValue)",
        )
    }
}