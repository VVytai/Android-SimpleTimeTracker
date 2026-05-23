package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.CommonViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.addBetweenEach
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import javax.inject.Inject

class RecordTagViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val commonViewDataMapper: CommonViewDataMapper,
) {

    // TODO also change selection in other places
    // TODO remove InfoViewData and mapSelectedHint in CommonViewDataMapper
    // typeId is empty - show all tags.
    suspend fun getViewData(
        selectedTags: List<RecordBase.Tag>,
        typeIds: List<Long>,
        showAllTags: Boolean,
        multipleChoiceAvailable: Boolean,
        showAddButton: Boolean,
        showBigEmptyHint: Boolean,
        showHint: Boolean,
        showArchived: Boolean,
        showUntaggedButton: Boolean,
        showAllTagsButton: Boolean,
    ): Result {
        fun List<RecordTag>.filterArchived(): List<RecordTag> {
            return if (showArchived) this else this.filterNot { it.archived }
        }

        val isDarkTheme = prefsInteractor.getDarkMode()
        val allTags = recordTagInteractor.getAll().filterArchived()

        if (allTags.isEmpty()) {
            return mapEmpty(
                showBigEmptyHint = showBigEmptyHint,
                showAddButton = showAddButton,
                isDarkTheme = isDarkTheme,
            )
        }

        val recordTags = getSelectableTagsInteractor.execute(*typeIds.toLongArray()).filterArchived()
        val recordTagIds = recordTags.map { it.id }
        val tagsFromOtherActivities = allTags.filter { it.id !in recordTagIds }
        val hasMoreTags = tagsFromOtherActivities.isNotEmpty()
        val types = recordTypeInteractor.getAll().associateBy { it.id }

        val selectedTagsMap = selectedTags.associateBy { it.tagId }
        val selectedTagIds = selectedTagsMap.keys
        val selected = allTags.filter { it.id in selectedTagIds }
        val available = recordTags.filter { it.id !in selectedTagIds }
        val availableFromOtherActivities = if (showAllTags) {
            tagsFromOtherActivities.filter { it.id !in selectedTagIds }
        } else {
            emptyList()
        }

        // Hint
        val hintViewData = if (showHint && selected.isEmpty()) {
            listOf(categoryViewDataMapper.mapToRecordTagHint())
        } else {
            emptyList()
        }

        // Selected
        val selectedViewData = mutableListOf<ViewHolderType>()
        if (selected.isNotEmpty()) {
            selectedViewData += if (multipleChoiceAvailable) {
                commonViewDataMapper.mapSelected()
            } else {
                commonViewDataMapper.mapPreselected()
            }
            selectedViewData += selected.map {
                categoryViewDataMapper.mapRecordTagWithValue(
                    tag = it,
                    tagData = selectedTagsMap[it.id],
                    types = types,
                    isDarkTheme = isDarkTheme,
                )
            }
        }

        // Available
        val availableViewData = mutableListOf<ViewHolderType>()
        if (available.isNotEmpty()) {
            availableViewData += commonViewDataMapper.mapAvailable()
        }
        categoryViewDataMapper.groupToTagGroups(available).forEach { (groupName, tags) ->
            if (groupName.isNotEmpty()) {
                availableViewData += HintViewData(
                    text = groupName,
                    paddingTop = 0,
                    paddingBottom = 0,
                )
            }

            availableViewData += tags.map {
                categoryViewDataMapper.mapRecordTag(
                    tag = it,
                    types = types,
                    isDarkTheme = isDarkTheme,
                )
            }
        }

        // From other
        val availableFromOtherViewData = mutableListOf<ViewHolderType>()
        if (availableFromOtherActivities.isNotEmpty()) {
            availableFromOtherViewData += HintViewData(
                text = resourceRepo.getString(R.string.change_record_tag_from_other_activity),
                paddingTop = 0,
                paddingBottom = 0,
            )

            availableFromOtherViewData += availableFromOtherActivities.map {
                categoryViewDataMapper.mapRecordTag(
                    tag = it,
                    types = types,
                    isDarkTheme = isDarkTheme,
                )
            }
        }

        // Buttons
        val buttonsViewData = mutableListOf<ViewHolderType>()
        if (showUntaggedButton) {
            buttonsViewData += categoryViewDataMapper.mapToUntaggedItem(
                isDarkTheme = isDarkTheme,
                isFiltered = false,
            )
        }
        if (showAllTagsButton && !showAllTags && hasMoreTags) {
            buttonsViewData += categoryViewDataMapper.mapToRecordTagShowAllItem(
                isDarkTheme = isDarkTheme,
            )
        }
        if (showAddButton) {
            buttonsViewData += categoryViewDataMapper.mapToRecordTagAddItem(isDarkTheme)
        }

        // All
        val viewData = listOf(
            hintViewData to true,
            selectedViewData to true,
            availableViewData to true,
            availableFromOtherViewData to true,
            buttonsViewData to (multipleChoiceAvailable || availableFromOtherViewData.isNotEmpty()),
        ).filter {
            it.first.isNotEmpty()
        }.addBetweenEach(
            map = { it.first },
            spacingProducer = { index, _, second ->
                if (second?.second == true) {
                    listOf(DividerViewData("divider_$index".hashCode().toLong()))
                } else {
                    null
                }
            },
        ).flatten().takeIf {
            it.isNotEmpty()
        } ?: listOf(commonViewDataMapper.mapSelectedHint(isEmpty = true))

        return Result(
            selectedCount = selected.size,
            data = viewData,
        )
    }

    private suspend fun mapEmpty(
        showBigEmptyHint: Boolean,
        showAddButton: Boolean,
        isDarkTheme: Boolean,
    ): Result {
        val viewData = mutableListOf<ViewHolderType>()
        viewData += if (showBigEmptyHint && recordTagInteractor.isEmpty()) {
            categoryViewDataMapper.mapToTagsFirstHint()
        } else {
            categoryViewDataMapper.mapToRecordTagsEmpty()
        }
        if (showAddButton) {
            viewData += categoryViewDataMapper.mapToRecordTagAddItem(isDarkTheme)
        }
        return Result(
            selectedCount = 0,
            data = viewData,
        )
    }

    data class Result(
        val selectedCount: Int,
        val data: List<ViewHolderType>,
    )
}
