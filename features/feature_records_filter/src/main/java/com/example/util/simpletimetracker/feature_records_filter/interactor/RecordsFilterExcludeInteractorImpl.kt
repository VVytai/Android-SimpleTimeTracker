package com.example.util.simpletimetracker.feature_records_filter.interactor

import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.feature_records_filter.api.RecordsFilterExcludeInteractor
import com.example.util.simpletimetracker.feature_records_filter.api.RecordsFilterExcludeInteractor.ExcludeType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterType
import javax.inject.Inject

class RecordsFilterExcludeInteractorImpl @Inject constructor(
    private val recordsFilterUpdateInteractor: RecordsFilterUpdateInteractor,
) : RecordsFilterExcludeInteractor {

    override fun exclude(
        id: Long,
        type: ExcludeType,
        currentFilters: List<RecordsFilter>,
    ): List<RecordsFilter> {
        // TODO change filter
        // TODO check tag consistency
        // TODO multitask?
        // TODO untracked?
        return when (type) {
            is ExcludeType.Activity -> {
                currentFilters
            }
            is ExcludeType.Category -> {
                currentFilters
            }
            is ExcludeType.Tag -> {
                recordsFilterUpdateInteractor.handleTagClick(
                    currentState = RecordFilterType.FilteredTags,
                    currentFilters = currentFilters,
                    itemId = id,
                )
            }
        }
    }
}