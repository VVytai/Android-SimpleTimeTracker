package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DaysInCalendar
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordsContainerUpdateInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordsShareUpdateInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordsUpdateInteractor
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_records.mapper.RecordsContainerOptionsListMapper
import com.example.util.simpletimetracker.feature_records.mapper.RecordsViewDataMapper
import com.example.util.simpletimetracker.feature_records.model.RecordsContainerOptionsListItem
import com.example.util.simpletimetracker.feature_records.model.RecordsContainerPosition
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordFromMainParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChartFilterDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordsContainerViewModel @Inject constructor(
    private val router: Router,
    private val timeMapper: TimeMapper,
    private val recordsViewDataMapper: RecordsViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
    private val recordsUpdateInteractor: RecordsUpdateInteractor,
    private val recordsContainerUpdateInteractor: RecordsContainerUpdateInteractor,
    private val recordsShareUpdateInteractor: RecordsShareUpdateInteractor,
    private val calendarToListShiftMapper: CalendarToListShiftMapper,
    private val recordsContainerOptionsListMapper: RecordsContainerOptionsListMapper,
) : BaseViewModel() {

    val title: LiveData<String>
        by lazySuspend { loadTitle(0) }
    val position: LiveData<RecordsContainerPosition>
        by lazySuspend { loadPosition(newPosition = 0, animate = false) }

    private var lastListShift: Int = 0
    private val currentPosition get() = position.value?.position.orZero()

    init {
        subscribeToUpdates()
    }

    fun onOptionsClick() = viewModelScope.launch {
        val items = recordsContainerOptionsListMapper.map()
        router.navigate(OptionsListParams(items))
    }

    fun onTodayClick() {
        viewModelScope.launch {
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
            val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
            val current = timeMapper.toTimestampShifted(
                rangesFromToday = getActualShift(),
                range = RangeLength.Day,
            )

            router.navigate(
                DateTimeDialogParams(
                    tag = DATE_TAG,
                    type = DateTimeDialogType.DATE,
                    timestamp = current,
                    useMilitaryTime = useMilitaryTime,
                    firstDayOfWeek = firstDayOfWeek,
                ),
            )
        }
    }

    fun onPreviousClick() {
        onRangeChanged(currentPosition - 1, animate = true)
    }

    fun onTodayLongClick() {
        onRangeChanged(0, animate = true)
    }

    fun onNextClick() {
        onRangeChanged(currentPosition + 1, animate = true)
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = viewModelScope.launch {
        when (tag) {
            DATE_TAG -> {
                timeMapper
                    .toTimestampShift(
                        toTime = timestamp,
                        range = RangeLength.Day,
                        firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
                    )
                    .toInt()
                    .let { shift ->
                        if (prefsInteractor.getShowRecordsCalendar()) {
                            calendarToListShiftMapper.mapListToCalendarShift(
                                listShift = shift,
                                daysInCalendar = prefsInteractor.getDaysInCalendar(),
                                startOfDayShift = prefsInteractor.getStartOfDayShift(),
                                firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
                            )
                        } else {
                            shift
                        }
                    }
                    .let { onRangeChanged(it, animate = true) }
            }
        }
    }

    fun onOptionsItemClick(id: OptionsListParams.Item.Id) {
        if (id !is RecordsContainerOptionsListItem) return
        when (id) {
            is RecordsContainerOptionsListItem.CalendarView -> onCalendarSwitchClick()
            is RecordsContainerOptionsListItem.Filter -> onFilterClick()
            is RecordsContainerOptionsListItem.Share -> onShareClick()
            is RecordsContainerOptionsListItem.Add -> onRecordAddClick()
        }
    }

    private fun onRecordAddClick() {
        viewModelScope.launch {
            val params = ChangeRecordParams.New(getActualShift())
            router.navigate(ChangeRecordFromMainParams(params))
        }
    }

    private fun onFilterClick() = viewModelScope.launch {
        val params = ChartFilterDialogParams(
            chartFilterType = prefsInteractor.getListFilterType(),
            filteredTypeIds = prefsInteractor.getFilteredTypesOnList(),
            filteredCategoryIds = prefsInteractor.getFilteredCategoriesOnList(),
            filteredTagIds = prefsInteractor.getFilteredTagsOnList(),
        )
        router.navigate(params)
    }

    private fun onShareClick() = viewModelScope.launch {
        recordsShareUpdateInteractor.sendShareClicked()
    }

    private fun onCalendarSwitchClick() = viewModelScope.launch {
        val newValue = !prefsInteractor.getShowRecordsCalendar()
        prefsInteractor.setShowRecordsCalendar(newValue)
        recalculateRangeOnCalendarViewSwitched()
        // Update record fragment that on the same page as was calendar,
        // other pages will be updated on becoming visible,
        // this one will not because it is already visible.
        recordsUpdateInteractor.send()
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            recordsContainerUpdateInteractor.showCalendarUpdated
                .collect { recalculateRangeOnCalendarViewSwitched() }
        }
        viewModelScope.launch {
            recordsContainerUpdateInteractor.calendarDaysUpdated
                .collect { recalculateRangeOnCalendarDaysChanged() }
        }
        viewModelScope.launch {
            recordsContainerUpdateInteractor.firstDayOfWeekUpdated
                .collect { updateTitle(currentPosition) }
        }
    }

    private suspend fun recalculateRangeOnCalendarViewSwitched() {
        val wasCalendar = position.value?.isCalendar.orFalse()
        val nowIsCalendar = prefsInteractor.getShowRecordsCalendar()
        if (wasCalendar == nowIsCalendar) return

        val newPosition = calendarToListShiftMapper.recalculateRangeOnCalendarViewSwitched(
            currentPosition = currentPosition,
            lastListPosition = lastListShift,
            showCalendar = prefsInteractor.getShowRecordsCalendar(),
            daysInCalendar = prefsInteractor.getDaysInCalendar(),
            startOfDayShift = prefsInteractor.getStartOfDayShift(),
            firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
        )
        onRangeChanged(newPosition, animate = false)
    }

    private suspend fun recalculateRangeOnCalendarDaysChanged() {
        val isCalendar = position.value?.isCalendar.orFalse()
        if (!isCalendar) return
        val prevDaysInCalendar = position.value?.daysInCalendar ?: DaysInCalendar.ONE
        val newDaysInCalendar = prefsInteractor.getDaysInCalendar()
        if (prevDaysInCalendar == newDaysInCalendar) return

        val newPosition = calendarToListShiftMapper.recalculateRangeOnCalendarDaysChanged(
            currentPosition = currentPosition,
            currentDaysInCalendar = prevDaysInCalendar,
            newDaysInCalendar = newDaysInCalendar,
            startOfDayShift = prefsInteractor.getStartOfDayShift(),
            firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
        )
        onRangeChanged(newPosition, animate = false)
    }

    private suspend fun getActualShift(): Int {
        val shift = currentPosition
        return if (prefsInteractor.getShowRecordsCalendar()) {
            calendarToListShiftMapper.mapCalendarToListShift(
                calendarShift = shift,
                daysInCalendar = prefsInteractor.getDaysInCalendar(),
                startOfDayShift = prefsInteractor.getStartOfDayShift(),
                firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
            ).end
        } else {
            shift
        }
    }

    private fun onRangeChanged(
        newPosition: Int,
        animate: Boolean,
    ) {
        updatePosition(newPosition, animate)
        updateTitle(newPosition)
    }

    private fun updatePosition(
        shift: Int,
        animate: Boolean,
    ) = viewModelScope.launch {
        val data = loadPosition(shift, animate)
        position.set(data)
    }

    private suspend fun loadPosition(
        newPosition: Int,
        animate: Boolean,
    ): RecordsContainerPosition {
        val isCalendar = prefsInteractor.getShowRecordsCalendar()
        if (!isCalendar) lastListShift = newPosition

        return RecordsContainerPosition(
            position = newPosition,
            isCalendar = isCalendar,
            daysInCalendar = prefsInteractor.getDaysInCalendar(),
            animate = animate,
        )
    }

    private fun updateTitle(newPosition: Int) = viewModelScope.launch {
        val data = loadTitle(newPosition)
        title.set(data)
    }

    private suspend fun loadTitle(shift: Int): String {
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val isCalendarView = prefsInteractor.getShowRecordsCalendar()
        val calendarDayCount = prefsInteractor.getDaysInCalendar()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

        return recordsViewDataMapper.mapTitle(
            shift = shift,
            startOfDayShift = startOfDayShift,
            isCalendarView = isCalendarView,
            daysInCalendar = calendarDayCount,
            firstDayOfWeek = firstDayOfWeek,
        )
    }

    companion object {
        private const val DATE_TAG = "records_date_tag"
    }
}
