package com.example.util.simpletimetracker.feature_records_filter.interactor

import com.example.util.simpletimetracker.domain.base.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.category.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.record.extension.getCategoryItems
import com.example.util.simpletimetracker.domain.record.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.record.extension.getTypeIds
import com.example.util.simpletimetracker.domain.record.extension.hasActivityFilter
import com.example.util.simpletimetracker.domain.record.extension.hasCategoryFilter
import com.example.util.simpletimetracker.domain.record.extension.hasTagsFilter
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_records_filter.api.RecordsFilterExcludeInteractor
import com.example.util.simpletimetracker.feature_records_filter.api.RecordsFilterExcludeInteractor.ExcludeType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterSelectionType
import javax.inject.Inject

class RecordsFilterExcludeInteractorImpl @Inject constructor(
    private val recordsFilterUpdateInteractor: RecordsFilterUpdateInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeToTagInteractor: RecordTypeToTagInteractor,
) : RecordsFilterExcludeInteractor {

    override suspend fun exclude(
        id: Long,
        type: ExcludeType,
        currentFilters: List<RecordsFilter>,
    ): List<RecordsFilter> {
        // TODO manually filter running records, untracked and multitask.
        // TODO multitasking filter by Read doesn't work.
        if (id == UNTRACKED_ITEM_ID) {
            return recordsFilterUpdateInteractor.handleUntrackedClick(currentFilters)
        }
        return when (type) {
            is ExcludeType.Activity -> {
                val itemIsSelected = currentFilters.hasActivityFilter() &&
                    id in currentFilters.getTypeIds()
                recordsFilterUpdateInteractor.handleTypeClick(
                    type = if (itemIsSelected) {
                        RecordFilterSelectionType.Select
                    } else {
                        RecordFilterSelectionType.Filter
                    },
                    id = id,
                    currentFilters = currentFilters,
                    recordTypes = recordTypeInteractor.getAll(),
                    recordTypeCategories = recordTypeCategoryInteractor.getAll(),
                    recordTags = recordTagInteractor.getAll(),
                    typesToTags = recordTypeToTagInteractor.getAll(),
                )
            }
            is ExcludeType.Category -> {
                val item = if (id == UNCATEGORIZED_ITEM_ID) {
                    RecordsFilter.CategoryItem.Uncategorized
                } else {
                    RecordsFilter.CategoryItem.Categorized(id)
                }
                val itemIsSelected = currentFilters.hasCategoryFilter() &&
                    item in currentFilters.getCategoryItems()
                recordsFilterUpdateInteractor.handleCategoryClick(
                    type = if (itemIsSelected) {
                        RecordFilterSelectionType.Select
                    } else {
                        RecordFilterSelectionType.Filter
                    },
                    id = id,
                    currentFilters = currentFilters,
                    recordTypes = recordTypeInteractor.getAll(),
                    recordTypeCategories = recordTypeCategoryInteractor.getAll(),
                    recordTags = recordTagInteractor.getAll(),
                    typesToTags = recordTypeToTagInteractor.getAll(),
                )
            }
            is ExcludeType.Tag -> {
                val item = if (id == UNCATEGORIZED_ITEM_ID) {
                    RecordsFilter.TagItem.Untagged
                } else {
                    RecordsFilter.TagItem.Tagged(id)
                }
                val itemIsSelected = currentFilters.hasTagsFilter() &&
                    item in currentFilters.getSelectedTags()
                recordsFilterUpdateInteractor.handleTagClick(
                    type = if (itemIsSelected) {
                        RecordFilterSelectionType.Select
                    } else {
                        RecordFilterSelectionType.Filter
                    },
                    currentFilters = currentFilters,
                    itemId = id,
                )
            }
        }
    }
}