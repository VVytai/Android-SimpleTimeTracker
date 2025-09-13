package com.example.util.simpletimetracker.feature_running_records.interactor

import com.example.util.simpletimetracker.core.interactor.ActivityFilterViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.ActivitySuggestionViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.interactor.GetRunningRecordViewDataMediator
import com.example.util.simpletimetracker.core.mapper.RecordShortcutViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.extension.plus
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordShortcut.interactor.RecordShortcutInteractor
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.EmptySpaceViewData
import com.example.util.simpletimetracker.feature_running_records.mapper.RunningRecordsViewDataMapper
import javax.inject.Inject

class RunningRecordsViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordInteractor: RecordInteractor,
    private val activityFilterViewDataInteractor: ActivityFilterViewDataInteractor,
    private val recordShortcutInteractor: RecordShortcutInteractor,
    private val mapper: RunningRecordsViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val recordShortcutViewDataMapper: RecordShortcutViewDataMapper,
    private val getRunningRecordViewDataMediator: GetRunningRecordViewDataMediator,
    private val getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor,
    private val filterGoalsByDayOfWeekInteractor: FilterGoalsByDayOfWeekInteractor,
    private val activitySuggestionViewDataInteractor: ActivitySuggestionViewDataInteractor,
) {

    suspend fun getViewData(
        completeTypeIds: Set<Long>,
        navBarHeightDp: Int,
    ): List<ViewHolderType> {
        val recordTypes = recordTypeInteractor.getAll()
        val recordTypesMap = recordTypes.associateBy(RecordType::id)
        val recordTags = recordTagInteractor.getAll()
        val runningRecords = runningRecordInteractor.getAll()
        val recordTypesRunning = runningRecords.map(RunningRecord::id)
        val shortcuts = recordShortcutInteractor.getAll()
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showFirstEnterHint = recordTypes.filterNot(RecordType::hidden).isEmpty()
        val showDefaultTypesButton = !prefsInteractor.getDefaultTypesHidden()
        val showPomodoroButton = prefsInteractor.getEnablePomodoroMode()
        val showRepeatButton = prefsInteractor.getEnableRepeatButton()
        val isPomodoroStarted = prefsInteractor.getPomodoroModeStartedTimestampMs() != 0L
        val retroactiveTrackingModeEnabled = prefsInteractor.getRetroactiveTrackingMode()
        val isFiltersCollapsed = prefsInteractor.getIsActivityFiltersCollapsed()
        val isNavBarAtTheBottom = prefsInteractor.getIsNavBarAtTheBottom()
        val goals = filterGoalsByDayOfWeekInteractor
            .execute(recordTypeGoalInteractor.getAllTypeGoals())
            .groupBy { it.idData.value }
        val allDailyCurrents = if (goals.isNotEmpty()) {
            getCurrentRecordsDurationInteractor.getAllDailyCurrents(
                typeIds = recordTypesMap.keys.toList(),
                runningRecords = runningRecords,
            )
        } else {
            // No goals - no need to calculate durations.
            emptyMap()
        }

        val runningRecordsViewData = when {
            showFirstEnterHint -> {
                listOf(mapper.mapToTypesEmpty())
            }
            retroactiveTrackingModeEnabled -> {
                val prevRecord = recordInteractor.getAllPrev(
                    timeStarted = System.currentTimeMillis(),
                )
                mapper.mapToRetroActiveMode(
                    typesMap = recordTypesMap,
                    recordTags = recordTags,
                    prevRecords = prevRecord,
                    isDarkTheme = isDarkTheme,
                    useProportionalMinutes = useProportionalMinutes,
                    useMilitaryTime = useMilitaryTime,
                    showSeconds = showSeconds,
                )
            }
            runningRecords.isEmpty() -> {
                listOf(mapper.mapToEmpty())
            }
            else -> {
                runningRecords
                    .sortedByDescending(RunningRecord::timeStarted)
                    .mapNotNull { runningRecord ->
                        getRunningRecordViewDataMediator.execute(
                            type = recordTypesMap[runningRecord.id] ?: return@mapNotNull null,
                            tags = recordTags,
                            goals = goals[runningRecord.id].orEmpty(),
                            record = runningRecord,
                            nowIconVisible = false,
                            goalsVisible = true,
                            totalDurationVisible = true,
                            isDarkTheme = isDarkTheme,
                            useMilitaryTime = useMilitaryTime,
                            useProportionalMinutes = useProportionalMinutes,
                            showSeconds = showSeconds,
                        )
                    }
                    .plus(
                        mapper.mapToHasRunningRecords(),
                    )
            }
        }.let {
            it + DividerViewData("running_records_divider".hashCode().toLong())
        }

        val filter = activityFilterViewDataInteractor.getFilter()
        val filtersViewData = activityFilterViewDataInteractor.getFilterViewData(
            filter = filter,
            isDarkTheme = isDarkTheme,
            isFiltersCollapsed = isFiltersCollapsed,
            appendAddButton = true,
        ).let {
            if (it.isNotEmpty()) it + DividerViewData("filter_divider".hashCode().toLong()) else it
        }

        val suggestionsViewData = activitySuggestionViewDataInteractor.getSuggestionsViewData(
            recordTypesMap = recordTypesMap,
            goals = goals,
            runningRecords = runningRecords,
            allDailyCurrents = allDailyCurrents,
            completeTypeIds = completeTypeIds,
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
        ).let {
            if (it.isNotEmpty()) it + DividerViewData("suggestions_divider".hashCode().toLong()) else it
        }

        val recordTypesViewData = recordTypes
            .filterNot {
                it.hidden
            }
            .let { list ->
                activityFilterViewDataInteractor.applyFilter(list, filter)
            }
            .map {
                recordTypeViewDataMapper.mapFiltered(
                    recordType = it,
                    isFiltered = it.id in recordTypesRunning,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme,
                    checkState = recordTypeViewDataMapper.mapGoalCheckmark(
                        type = it,
                        goals = goals,
                        allDailyCurrents = allDailyCurrents,
                    ),
                    isComplete = it.id in completeTypeIds,
                )
            }
            .let { data ->
                mutableListOf<ViewHolderType>().apply {
                    data.let(::addAll)
                    if (showRepeatButton) {
                        recordTypeViewDataMapper.mapToRepeatItem(
                            numberOfCards = numberOfCards,
                            isDarkTheme = isDarkTheme,
                        ).let(::add)
                    }
                    if (showPomodoroButton) {
                        recordTypeViewDataMapper.mapToPomodoroItem(
                            numberOfCards = numberOfCards,
                            isDarkTheme = isDarkTheme,
                            isPomodoroStarted = isPomodoroStarted,
                        ).let(::add)
                    }
                    recordTypeViewDataMapper.mapToAddItem(
                        numberOfCards = numberOfCards,
                        isDarkTheme = isDarkTheme,
                    ).let(::add)
                    if (showDefaultTypesButton) {
                        recordTypeViewDataMapper.mapToAddDefaultItem(
                            numberOfCards = numberOfCards,
                            isDarkTheme = isDarkTheme,
                        ).let(::add)
                    }
                }
            }

        val shortcutsViewData = shortcuts.mapNotNull { shortcut ->
            recordShortcutViewDataMapper.map(
                shortcut = shortcut,
                recordType = recordTypesMap[shortcut.typeId] ?: return@mapNotNull null,
                recordTags = recordTags,
                isDarkTheme = isDarkTheme,
            )
        }.takeIf {
            it.isNotEmpty()
        }?.let {
            listOf(DividerViewData("shortcuts_divider".hashCode().toLong())) + it
        }

        // Flexbox layout doesn't fully support clipToPadding = false.
        // Because of that bottom padding with nav bar insets are not applied to main recycler.
        // Instead an empty space added.
        val bottomSpaceForNavBar = if (isNavBarAtTheBottom) {
            emptyList()
        } else {
            EmptySpaceViewData(
                id = "running_records_nav_bar_space".hashCode().toLong(),
                height = EmptySpaceViewData.ViewDimension.ExactSizeDp(navBarHeightDp),
                wrapBefore = true,
            ).let(::listOf)
        }

        return runningRecordsViewData +
            filtersViewData +
            suggestionsViewData +
            recordTypesViewData +
            shortcutsViewData +
            bottomSpaceForNavBar
    }
}