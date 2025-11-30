package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.RecordShortcutViewDataMapper
import com.example.util.simpletimetracker.domain.extension.search
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordShortcut.interactor.RecordShortcutInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordShortcut.RecordShortcutViewData
import javax.inject.Inject

class RecordsShortcutsViewDataInteractor @Inject constructor(
    private val recordShortcutInteractor: RecordShortcutInteractor,
    private val recordShortcutViewDataMapper: RecordShortcutViewDataMapper,
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
        }.mapNotNull { shortcut ->
            val isRunning = runningRecordsProcessed.any { runningRecord ->
                runningRecord.id == shortcut.typeId &&
                    runningRecord.comment == shortcut.comment &&
                    runningRecord.tags == shortcut.tags.sortedBy { it.tagId }
            }
            recordShortcutViewDataMapper.map(
                shortcut = shortcut,
                recordType = recordTypesMap[shortcut.typeId] ?: return@mapNotNull null,
                recordTags = recordTags,
                isDarkTheme = isDarkTheme,
                isFiltered = isRunning,
            )
        }.search(
            text = searchText,
            searchableContent = { data.name },
        )
    }
}