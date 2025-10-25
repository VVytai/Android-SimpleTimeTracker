package com.example.util.simpletimetracker.feature_records.mapper

import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.mapper.DaysInCalendarMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DaysInCalendar
import com.example.util.simpletimetracker.domain.extension.padDuration
import com.example.util.simpletimetracker.feature_base_adapter.InfiniteRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.dateSelector.DateSelectorDayViewData
import com.example.util.simpletimetracker.feature_base_adapter.dateSelector.DateSelectorRangeViewData
import java.util.Calendar
import javax.inject.Inject

class DateSelectorMapper @Inject constructor(
    private val timeMapper: TimeMapper,
    private val daysInCalendarMapper: DaysInCalendarMapper,
    private val calendarToListShiftMapper: CalendarToListShiftMapper,
) : InfiniteRecyclerAdapter.DataProvider {

    var currentSelectedPosition: Int = 0

    private var isInitialized: Boolean = false
    private var startOfDayShift: Long = 0
    private var isCalendarView: Boolean = false
    private var daysInCalendar: DaysInCalendar = DaysInCalendar.ONE
    private var firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY
    private var isCalendar: Boolean = false
    private var currentItem: InfiniteRecyclerAdapter.Data = getItem(0)

    override fun isInitialized(): Boolean {
        return isInitialized
    }

    override fun getCurrentItem(): InfiniteRecyclerAdapter.Data {
        return currentItem
    }

    override fun getItem(
        position: Int,
    ): InfiniteRecyclerAdapter.Data {
        return if (isCalendar) {
            getRangeViewDataData(position)
        } else {
            getDayViewDataData(position)
        }
    }

    fun setup(
        startOfDayShift: Long,
        isCalendarView: Boolean,
        daysInCalendar: DaysInCalendar,
        firstDayOfWeek: DayOfWeek,
    ) {
        this.startOfDayShift = startOfDayShift
        this.isCalendarView = isCalendarView
        this.daysInCalendar = daysInCalendar
        this.firstDayOfWeek = firstDayOfWeek

        this.isCalendar = isCalendar(
            isCalendarView = isCalendarView,
            daysInCalendar = daysInCalendar,
        )
        currentItem = getItem(0)

        this.isInitialized = true
    }

    private fun getDayViewDataData(
        position: Int,
    ): DateSelectorDayViewData {
        val calendar = getCalendar(position)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            .let(timeMapper::toDayOfWeek)
            .let(timeMapper::toShortDayOfWeekName)
        val dayOfMoth = calendar.get(Calendar.DAY_OF_MONTH).toString()

        val isToday = position == 0
        val isFuture = position > 0
        val isSelected = position == currentSelectedPosition

        return DateSelectorDayViewData(
            position = position,
            dayMonth = DateSelectorDayViewData.DayMonth(
                topText = dayOfWeek,
                bottomText = dayOfMoth,
            ),
            cardData = DateSelectorDayViewData.CardData(
                isToday = isToday,
                isSelected = isSelected,
                isFuture = isFuture,
            ),
        )
    }

    private fun getCalendar(
        position: Int
    ): Calendar {
        return Calendar.getInstance().apply {
            // Shifted by startOfDayShift, so that for example:
            // now it is 01:00 17.10.2025 but today starts at 2:00,
            // it would show 16.10.2025.
            timeInMillis = System.currentTimeMillis() - startOfDayShift
            add(Calendar.DATE, position)
        }
    }

    private fun getRangeViewDataData(
        position: Int,
    ): DateSelectorRangeViewData {
        fun DateSelectorDayViewData.DayMonth.pad(): DateSelectorDayViewData.DayMonth {
            return copy(
                bottomText = this.bottomText.padDuration(),
            )
        }

        val calendarRange = calendarToListShiftMapper.mapCalendarToListShift(
            calendarShift = position,
            daysInCalendar = daysInCalendar,
            startOfDayShift = startOfDayShift,
            firstDayOfWeek = firstDayOfWeek,
        )

        fun getDayViewData(position: Int): DateSelectorDayViewData.DayMonth {
            val calendar = getCalendar(position)
            val month = timeMapper.formatShortMonth(calendar.timeInMillis)
            val dayOfMoth = calendar.get(Calendar.DAY_OF_MONTH).toString()
            return DateSelectorDayViewData.DayMonth(
                topText = month,
                bottomText = dayOfMoth,
            )
        }

        val date1 = getDayViewData(calendarRange.start)
        val date2 = getDayViewData(calendarRange.end)

        val isToday = position == 0
        val isFuture = position > 0
        val isSelected = position == currentSelectedPosition

        return DateSelectorRangeViewData(
            position = position,
            dayMonth1 = date1.pad(),
            dayMonth2 = date2.pad(),
            cardData = DateSelectorDayViewData.CardData(
                isToday = isToday,
                isSelected = isSelected,
                isFuture = isFuture,
            ),
        )
    }

    private fun isCalendar(
        isCalendarView: Boolean,
        daysInCalendar: DaysInCalendar,
    ): Boolean {
        val calendarDayCount = daysInCalendarMapper.mapDaysCount(daysInCalendar)
        return isCalendarView && calendarDayCount > 1
    }
}