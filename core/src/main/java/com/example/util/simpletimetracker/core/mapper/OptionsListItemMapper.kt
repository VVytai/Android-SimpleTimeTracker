package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.domain.base.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import javax.inject.Inject

class OptionsListItemMapper @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
) {

    fun isIconCheckVisible(
        filteredIds: List<Long>,
        existingIds: Map<Long, Boolean>,
    ): Boolean {
        return filteredIds.any {
            val isSpecial = it in listOf(UNTRACKED_ITEM_ID, UNCATEGORIZED_ITEM_ID)
            val notRemoved = it in existingIds
            !isSpecial && notRemoved
        }
    }

    // Map is faster for contains.
    suspend fun getExistingIds(
        chartFilterType: ChartFilterType,
    ): Map<Long, Boolean> {
        return when (chartFilterType) {
            ChartFilterType.ACTIVITY -> recordTypeInteractor.getAll().map { it.id }
            ChartFilterType.CATEGORY -> categoryInteractor.getAll().map { it.id }
            ChartFilterType.RECORD_TAG -> recordTagInteractor.getAll().map { it.id }
        }.associateWith { true }
    }
}