package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import javax.inject.Inject

class ShouldCloseAfterOneTagInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun execute(typeId: Long): Boolean {
        if (!prefsInteractor.getRecordTagSelectionCloseAfterOne()) return false
        val excludedActivities = prefsInteractor.getCloseAfterOneTagExcludeActivities().toSet()
        return typeId !in excludedActivities
    }
}