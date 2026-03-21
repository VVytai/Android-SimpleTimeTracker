package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import javax.inject.Inject

class OnSettingsShortcutClickInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun execute(action: RecordShortcut.SettingAction) {
        when (action) {
            RecordShortcut.SettingAction.Multitasking -> {
                val newValue = !prefsInteractor.getAllowMultitasking()
                prefsInteractor.setAllowMultitasking(newValue)
                onAllowMultitaskingChange()
            }
            RecordShortcut.SettingAction.RetroactiveMode -> {
                val newValue = !prefsInteractor.getRetroactiveTrackingMode()
                prefsInteractor.setRetroactiveTrackingMode(newValue)
                onRetroactiveTrackingModeChange()
            }
        }
    }

    suspend fun onAllowMultitaskingChange() {
        externalViewsInteractor.onAllowMultitaskingChange()
    }

    suspend fun onRetroactiveTrackingModeChange() {
        runningRecordInteractor.getAll().forEach {
            removeRunningRecordMediator.removeWithRecordAdd(it)
        }
        // TODO do not update widgets if there was running records?
        externalViewsInteractor.onRetroactiveTrackingModeChange()
    }
}