package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.CommonViewDataMapper
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.recordTag.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.info.InfoViewData
import javax.inject.Inject

class RecordTagViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val commonViewDataMapper: CommonViewDataMapper,
) {

    // typeId == null - show all tags.
    suspend fun getViewData(
        selectedTags: List<Long>,
        typeId: Long?,
        multipleChoiceAvailable: Boolean,
        showAddButton: Boolean,
        showArchived: Boolean,
        showUntaggedButton: Boolean,
        showAllTagsButton: Boolean,
    ): Result {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val recordTags = if (typeId != null) {
            getSelectableTagsInteractor.execute(typeId)
        } else {
            recordTagInteractor.getAll()
        }
        val types = recordTypeInteractor.getAll().associateBy { it.id }
        val data = recordTags
            .let { tags -> if (showArchived) tags else tags.filterNot { it.archived } }

        return if (data.isNotEmpty()) {
            val selected = data.filter { it.id in selectedTags }
            val available = data.filter { it.id !in selectedTags }

            val viewData = mutableListOf<ViewHolderType>()

            viewData += listOf(
                categoryViewDataMapper.mapToRecordTagHint(),
                DividerViewData(1),
            ).takeIf { showAddButton }

            viewData += commonViewDataMapper.mapSelectedHint(
                isEmpty = selected.isEmpty(),
            ).takeIf { multipleChoiceAvailable }

            viewData += selected.map {
                categoryViewDataMapper.mapRecordTag(
                    tag = it,
                    type = types[it.iconColorSource],
                    isDarkTheme = isDarkTheme,
                )
            }

            viewData += DividerViewData(2)
                .takeUnless { available.isEmpty() }
                .takeIf { multipleChoiceAvailable }

            categoryViewDataMapper.groupToTagGroups(available).forEach { (groupName, tags) ->
                if (groupName.isNotEmpty()) {
                    viewData += InfoViewData(text = groupName)
                }

                viewData += tags.map {
                    categoryViewDataMapper.mapRecordTag(
                        tag = it,
                        type = types[it.iconColorSource],
                        isDarkTheme = isDarkTheme,
                    )
                }
            }

            if (showUntaggedButton) {
                if (selected.isNotEmpty() || available.isNotEmpty()) {
                    viewData += DividerViewData(3)
                        .takeIf { multipleChoiceAvailable }
                    viewData += categoryViewDataMapper.mapToUntaggedItem(
                        isDarkTheme = isDarkTheme,
                        isFiltered = false,
                    )
                }
            }

            if (showAllTagsButton) {
                viewData += categoryViewDataMapper.mapToRecordTagShowAllItem(
                    isDarkTheme = isDarkTheme,
                )
            }

            viewData += categoryViewDataMapper.mapToRecordTagAddItem(isDarkTheme)
                .takeIf { showAddButton }

            Result(
                selectedCount = selected.size,
                data = viewData,
            )
        } else {
            val viewData = mutableListOf<ViewHolderType>()
            viewData += if (showAddButton && recordTagInteractor.isEmpty()) {
                categoryViewDataMapper.mapToTagsFirstHint()
            } else {
                categoryViewDataMapper.mapToRecordTagsEmpty()
            }
            viewData += categoryViewDataMapper.mapToRecordTagAddItem(isDarkTheme)
                .takeIf { showAddButton }
            Result(
                selectedCount = 0,
                data = viewData,
            )
        }
    }

    data class Result(
        val selectedCount: Int,
        val data: List<ViewHolderType>,
    )
}