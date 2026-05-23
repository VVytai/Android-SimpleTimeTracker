package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.info.InfoViewData
import javax.inject.Inject

class CommonViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

     fun mapRecordsCountHint(count: Int): String {
        // Ex. "Selected 5 Records"
        val recordsSelectedString = resourceRepo.getString(
            R.string.separator_template,
            count,
            resourceRepo.getQuantityString(
                R.plurals.statistics_detail_times_tracked,
                count,
            ).lowercase(),
        )
        val text = resourceRepo.getString(
            R.string.separator_template,
            resourceRepo.getString(R.string.something_selected),
            recordsSelectedString,
        )
        return text
    }

    fun mapSelectedHint(isEmpty: Boolean): ViewHolderType {
        return InfoViewData(
            text = if (isEmpty) {
                R.string.nothing_selected
            } else {
                R.string.something_selected
            }.let(resourceRepo::getString),
        )
    }
}