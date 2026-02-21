package com.example.util.simpletimetracker.core.interactor

import javax.inject.Inject

class IsMultipleTagChoiceAvailableInteractor @Inject constructor(
    private val shouldCloseAfterOneTagInteractor: ShouldCloseAfterOneTagInteractor,
) {

    fun execute(
        typeId: Long,
        hasPreselectedTags: Boolean,
        closeAfterOne: Boolean,
        excludedActivities: Set<Long>,
    ): Boolean {
        val shouldCloseAfterOne = shouldCloseAfterOneTagInteractor.execute(
            typeId = typeId,
            closeAfterOne = closeAfterOne,
            excludedActivities = excludedActivities,
        )
        // If there are preselected tags - ignore setting.
        return hasPreselectedTags || !shouldCloseAfterOne
    }
}