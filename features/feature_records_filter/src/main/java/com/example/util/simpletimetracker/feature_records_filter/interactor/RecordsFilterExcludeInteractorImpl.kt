package com.example.util.simpletimetracker.feature_records_filter.interactor

import com.example.util.simpletimetracker.domain.base.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.category.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.record.extension.getCategoryItems
import com.example.util.simpletimetracker.domain.record.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.record.extension.getTypeIds
import com.example.util.simpletimetracker.domain.record.extension.hasSelectedActivityFilter
import com.example.util.simpletimetracker.domain.record.extension.hasSelectedCategoryFilter
import com.example.util.simpletimetracker.domain.record.extension.hasSelectedTagsFilter
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
        // Shouldn't be possible
        if (id == UNTRACKED_ITEM_ID) return currentFilters

        return when (type) {
            is ExcludeType.Activity -> {
                val itemIsSelected = currentFilters.hasSelectedActivityFilter() &&
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
                val itemIsSelected = currentFilters.hasSelectedCategoryFilter() &&
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
                val itemIsSelected = currentFilters.hasSelectedTagsFilter() &&
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

    override suspend fun excludeOther(
        id: Long,
        type: ExcludeType,
    ): List<RecordsFilter> {
        if (id == UNTRACKED_ITEM_ID) return listOf(RecordsFilter.Untracked)

        return when (type) {
            is ExcludeType.Activity -> {
                RecordsFilter.Activity(selected = listOf(id), filtered = emptyList())
            }
            is ExcludeType.Category -> {
                val item = if (id == UNCATEGORIZED_ITEM_ID) {
                    RecordsFilter.CategoryItem.Uncategorized
                } else {
                    RecordsFilter.CategoryItem.Categorized(id)
                }
                RecordsFilter.Category(selected = listOf(item), filtered = emptyList())
            }
            is ExcludeType.Tag -> {
                val item = if (id == UNCATEGORIZED_ITEM_ID) {
                    RecordsFilter.TagItem.Untagged
                } else {
                    RecordsFilter.TagItem.Tagged(id)
                }
                RecordsFilter.Tags(selected = listOf(item), filtered = emptyList())
            }
        }.let(::listOf)
    }
}