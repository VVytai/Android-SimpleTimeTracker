package com.example.util.simpletimetracker.core.delegates.dateSelector.viewModelDelegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.dateSelector.mapper.DateSelectorMapper
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.InfiniteRecyclerAdapter
import javax.inject.Inject

class DateSelectorViewModelDelegate @Inject constructor(
    val dataProvider: DateSelectorMapper,
    private val prefsInteractor: PrefsInteractor,
) : ViewModelDelegate() {

    val dateScrollPosition: LiveData<Int> = SingleLiveEvent<Int>()
    val updateDatesViewData: LiveData<Unit> = SingleLiveEvent<Unit>()

    var scrollWasAlreadyRequested: Boolean = false

    private var parent: Parent? = null

    fun attach(parent: Parent) {
        this.parent = parent
    }

    suspend fun initialize() {
        setupDatesSelector()
        updateDatesViewData.set(Unit)
        dateScrollPosition.set(0)
    }

    fun onDateClick(item: InfiniteRecyclerAdapter.Data) {
        if (parent?.currentPosition == item.position) {
            parent?.onSelectDateClick()
        } else {
            parent?.onRangeChanged(item.position, animate = true)
        }
    }

    fun onDateLongClick(item: InfiniteRecyclerAdapter.Data) {
        if (parent?.currentPosition == item.position) {
            parent?.onRangeChanged(0, animate = true)
        } else {
            onDateClick(item)
        }
    }

    fun onScrolledToDate(position: Int) {
        if (position != parent?.currentPosition) {
            parent?.onRangeChanged(position, animate = true)
        }
    }

    fun updatePosition(shift: Int) {
        dataProvider.currentSelectedPosition = shift
        updateDatesViewData.set(Unit)
        dateScrollPosition.set(shift)
    }

    suspend fun recalculateRangeOnCalendarViewSwitched() {
        setupDatesSelector()
    }

    private suspend fun setupDatesSelector() {
        dataProvider.setup(
            startOfDayShift = prefsInteractor.getStartOfDayShift(),
            isCalendarView = prefsInteractor.getShowRecordsCalendar(),
            daysInCalendar = prefsInteractor.getDaysInCalendar(),
            firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
        )
    }

    interface Parent {
        val currentPosition: Int

        fun onSelectDateClick()
        fun onRangeChanged(newPosition: Int, animate: Boolean)
    }
}