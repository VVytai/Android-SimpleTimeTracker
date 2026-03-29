package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.RecordShortcutViewDataMapper
import com.example.util.simpletimetracker.domain.extension.search
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordShortcut.interactor.RecordShortcutInteractor
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.recordShortcut.RecordShortcutViewData
import javax.inject.Inject

class RecordsShortcutsViewDataInteractor @Inject constructor(
    private val recordShortcutInteractor: RecordShortcutInteractor,
    private val recordShortcutViewDataMapper: RecordShortcutViewDataMapper,
    private val isSettingShortcutEnabledInteractor: IsSettingShortcutEnabledInteractor,
    private val activityFilterViewDataInteractor: ActivityFilterViewDataInteractor,
) {

    suspend fun getShortcutsViewData(
        filter: ActivityFilterViewDataInteractor.Filter,
        recordTypesMap: Map<Long, RecordType>,
        recordTags: List<RecordTag>,
        runningRecords: List<RunningRecord>,
        searchText: String,
        isDarkTheme: Boolean,
    ): List<RecordShortcutViewData> {
        val shortcuts = recordShortcutInteractor.getAll()

        val runningRecordsProcessed = runningRecords.map { runningRecord ->
            runningRecord.copy(tags = runningRecord.tags.sortedBy { it.tagId })
        }

        return shortcuts.let {
            activityFilterViewDataInteractor.applyFilterToShortcuts(it, filter)
        }.map { shortcut ->
            val isFiltered = when (val target = shortcut.target) {
                is RecordShortcut.Target.Record -> {
                    runningRecordsProcessed.any { runningRecord ->
                        runningRecord.id == target.typeId &&
                            runningRecord.comment == target.comment &&
                            runningRecord.tags == target.tags.sortedBy { it.tagId }
                    }
                }
                is RecordShortcut.Target.Setting -> false
            }
            val isEnabled = when (val target = shortcut.target) {
                is RecordShortcut.Target.Record -> false
                is RecordShortcut.Target.Setting -> {
                    isSettingShortcutEnabledInteractor.execute(target)
                }
            }
            recordShortcutViewDataMapper.map(
                shortcut = shortcut,
                typesMap = recordTypesMap,
                tags = recordTags,
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
                isEnabled = isEnabled,
            )
        }.search(
            text = searchText,
            searchableContent = { data.name },
        )
    }
}