package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.delegates.dateSelector.viewModelDelegate.DateSelectorViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.core.viewData.SelectDateViewData
import com.example.util.simpletimetracker.core.viewData.SelectLastDaysViewData
import com.example.util.simpletimetracker.core.viewData.SelectRangeViewData
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.StatisticsUpdateInteractor
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics.mapper.StatisticsContainerOptionsListMapper
import com.example.util.simpletimetracker.feature_statistics.model.StatisticsContainerOptionsListItem
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.CustomRangeSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsContainerViewModel @Inject constructor(
    val dateSelectorViewModelDelegate: DateSelectorViewModelDelegate,
    private val router: Router,
    private val timeMapper: TimeMapper,
    private val rangeViewDataMapper: RangeViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsUpdateInteractor: StatisticsUpdateInteractor,
    private val statisticsContainerOptionsListMapper: StatisticsContainerOptionsListMapper,
) : ViewModel() {

    val position: LiveData<Int> by lazy {
        return@lazy MutableLiveData(0)
    }

    val rangeItems: LiveData<RangesViewData> by lazy {
        return@lazy MutableLiveData<RangesViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadRanges() }
            initial
        }
    }

    val selectRangeClick: LiveData<Unit> = SingleLiveEvent<Unit>()

    private var rangeLength: RangeLength? = null
    private val currentPosition: Int get() = position.value.orZero()

    init {
        dateSelectorViewModelDelegate.attach(getDateSelectorDelegateParent())
    }

    fun initialize() {
        viewModelScope.launch {
            dateSelectorViewModelDelegate.initialize()
        }
    }

    // TODO DATE update on firstDayOfWeek change

    fun onOptionsClick() = viewModelScope.launch {
        val items = statisticsContainerOptionsListMapper.map()
        router.navigate(OptionsListParams(items))
    }

    fun onOptionsLongClick() = viewModelScope.launch {
        statisticsUpdateInteractor.sendFilterClicked()
    }

    fun onRangeSelected(item: CustomSpinner.CustomSpinnerItem) {
        when (item) {
            is SelectDateViewData -> {
                onSelectDateClick()
                updateRanges()
            }
            is SelectRangeViewData -> {
                onSelectRangeClick()
                updateRanges()
            }
            is SelectLastDaysViewData -> {
                onSelectLastDaysClick()
                updateRanges()
            }
        }
    }

    fun onRangeUpdated(newRange: RangeLength) {
        if (newRange != rangeLength) {
            rangeLength = newRange
            updatePosition(0)
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = viewModelScope.launch {
        when (tag) {
            DATE_TAG -> {
                timeMapper.toTimestampShift(
                    toTime = timestamp,
                    range = prefsInteractor.getStatisticsRange(),
                    firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
                ).toInt().let(::updatePosition)
            }
        }
    }

    fun onOptionsItemClick(id: OptionsListParams.Item.Id) = viewModelScope.launch {
        if (id !is StatisticsContainerOptionsListItem) return@launch
        when (id) {
            is StatisticsContainerOptionsListItem.Filter -> {
                statisticsUpdateInteractor.sendFilterClicked()
            }
            is StatisticsContainerOptionsListItem.Share -> {
                statisticsUpdateInteractor.sendShareClicked()
            }
            is StatisticsContainerOptionsListItem.BackToToday -> {
                onBackToTodayClick()
            }
            is StatisticsContainerOptionsListItem.SelectDate -> {
                onSelectDateClick()
            }
            is StatisticsContainerOptionsListItem.SelectRange -> {
                selectRangeClick.set(Unit)
            }
        }
    }

    private fun onBackToTodayClick() {
        updatePosition(0)
    }

    private fun onSelectDateClick() = viewModelScope.launch {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val current = timeMapper.toTimestampShifted(
            rangesFromToday = currentPosition,
            range = prefsInteractor.getStatisticsRange(),
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

    private fun onSelectRangeClick() = viewModelScope.launch {
        val currentCustomRange = (prefsInteractor.getStatisticsRange() as? RangeLength.Custom)?.range

        CustomRangeSelectionParams(
            rangeStart = currentCustomRange?.timeStarted,
            rangeEnd = currentCustomRange?.timeEnded,
        ).let(router::navigate)
    }

    private fun onSelectLastDaysClick() = viewModelScope.launch {
        DurationDialogParams(
            tag = LAST_DAYS_COUNT_TAG,
            value = DurationDialogParams.Value.Count(
                getCurrentLastDaysCount().toLong(),
            ),
            hideDisableButton = true,
        ).let(router::navigate)
    }

    private suspend fun getCurrentLastDaysCount(): Int {
        return prefsInteractor.getStatisticsLastDays()
    }

    private suspend fun getRangeLength(): RangeLength {
        return rangeLength ?: prefsInteractor.getStatisticsRange()
    }

    private fun getDateSelectorDelegateParent(): DateSelectorViewModelDelegate.Parent {
        return object : DateSelectorViewModelDelegate.Parent {
            override val currentPosition: Int
                get() = this@StatisticsContainerViewModel.currentPosition

            override fun onDateClick() {
                selectRangeClick.set(Unit)
            }

            override fun onRangeChanged(newPosition: Int) =
                this@StatisticsContainerViewModel.updatePosition(newPosition)
        }
    }

    private fun updatePosition(newPosition: Int) {
        dateSelectorViewModelDelegate.updatePosition(newPosition)
        position.set(newPosition)
        updateRanges()
    }

    private fun updateRanges() = viewModelScope.launch {
        rangeItems.set(loadRanges())
    }

    private suspend fun loadRanges(): RangesViewData {
        return rangeViewDataMapper.mapToRanges(
            currentRange = getRangeLength(),
            addSelection = true,
            lastDaysCount = getCurrentLastDaysCount(),
        )
    }

    companion object {
        const val LAST_DAYS_COUNT_TAG = "statistics_last_days_count_tag"
        private const val DATE_TAG = "statistics_date_tag"
    }
}
