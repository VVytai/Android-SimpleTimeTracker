package com.example.util.simpletimetracker.feature_tag_selection.interactor

import com.example.util.simpletimetracker.core.interactor.RecordTagViewDataInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import javax.inject.Inject

class RecordTagSelectionViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTagViewDataInteractor: RecordTagViewDataInteractor,
) {

    suspend fun getViewData(
        typeId: Long,
        selectedTags: List<Long>,
        showAllTags: Boolean,
    ): List<ViewHolderType> {
        val closeAfterOneTagSelected: Boolean = prefsInteractor.getRecordTagSelectionCloseAfterOne()
        val result: MutableList<ViewHolderType> = mutableListOf()

        recordTagViewDataInteractor.getViewData(
            selectedTags = selectedTags,
            typeId = if (showAllTags) null else typeId,
            multipleChoiceAvailable = !closeAfterOneTagSelected,
            showAddButton = false,
            showArchived = false,
            showUntaggedButton = true,
            showAllTagsButton = !showAllTags,
        ).data.let(result::addAll)

        return result
    }
}