package com.example.util.simpletimetracker.feature_records.mapper

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.feature_base_adapter.dateSelector.DateSelectorDataMapper
import java.util.Calendar
import javax.inject.Inject

class DateSelectorMapper @Inject constructor(
    private val timeMapper: TimeMapper,
) : DateSelectorDataMapper {

    var currentSelectedPosition: Int = 0

    override fun mapData(position: Int): DateSelectorDataMapper.Data {
        val calendar = Calendar.getInstance()
            .apply { add(Calendar.DATE, position) }
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            .let(timeMapper::toDayOfWeek)
            .let(timeMapper::toShortDayOfWeekName)
        val dayOfMoth = calendar.get(Calendar.DAY_OF_MONTH).toString()
        val isToday = position == 0
        val isFuture = position > 0
        val isSelected = position == currentSelectedPosition

        return DateSelectorDataMapper.Data(
            dayOfWeek = dayOfWeek,
            dayOfMonth = dayOfMoth,
            isToday = isToday,
            isSelected = isSelected,
            isFuture = isFuture
        )
    }
}