package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.common.R
import com.example.util.simpletimetracker.core.repo.BaseResourceRepo
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.padDuration
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import java.util.Calendar
import javax.inject.Inject

class RangeTitleMapper @Inject constructor(
    private val resourceRepo: BaseResourceRepo,
    private val timeMapper: TimeMapper,
) {

    // TODO DATE refactor, remove?
    fun mapToTitle(
        rangeLength: RangeLength,
        position: Int,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
        useShortCustomRange: Boolean = false,
    ): String {
        return when (rangeLength) {
            is RangeLength.Day -> timeMapper.toDayTitle(position, startOfDayShift)
            is RangeLength.Week -> timeMapper.toWeekTitle(position, startOfDayShift, firstDayOfWeek)
            is RangeLength.Month -> timeMapper.toMonthTitle(position, startOfDayShift)
            is RangeLength.Year -> timeMapper.toYearTitle(position, startOfDayShift)
            is RangeLength.All -> resourceRepo.getString(R.string.range_overall)
            is RangeLength.Last -> if (position == 0) {
                mapToLastDaysTitle(rangeLength.days)
            } else {
                mapToCustomRangeTitle(rangeLength, position, startOfDayShift, firstDayOfWeek)
            }
            is RangeLength.Custom -> if (useShortCustomRange) {
                mapToSelectRangeName()
            } else {
                mapToCustomRangeTitle(rangeLength, position, startOfDayShift, firstDayOfWeek)
            }
        }
    }

    fun mapToDateSelectorData(
        rangeLength: RangeLength,
        position: Int,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
    ): DateSelectorData {
        val calendar = Calendar.getInstance()
        return when (rangeLength) {
            is RangeLength.Day -> {
                calendar.timeInMillis = timeMapper.toDayDateTimestamp(position, startOfDayShift)
                val data = DateSelectorData.Data(
                    topText = calendar.get(Calendar.DAY_OF_WEEK)
                        .let(timeMapper::toDayOfWeek)
                        .let(timeMapper::toShortDayOfWeekName),
                    bottomText = calendar.get(Calendar.DAY_OF_MONTH).toString(),
                )
                DateSelectorData.Single(data)
            }
            is RangeLength.Week -> {
                val (start, end) = timeMapper.toWeekDateTimestamp(position, startOfDayShift, firstDayOfWeek)
                DateSelectorData.Double(
                    data1 = mapToDateSelectorDayOfMonthData(start),
                    data2 = mapToDateSelectorDayOfMonthData(end),
                )
            }
            is RangeLength.Month -> {
                calendar.timeInMillis = timeMapper.toMonthDateTimestamp(position, startOfDayShift)
                val data = DateSelectorData.Data(
                    topText = "",
                    bottomText = timeMapper.formatShortMonth(calendar.timeInMillis),
                )
                DateSelectorData.Single(data)
            }
            is RangeLength.Year -> {
                calendar.timeInMillis = timeMapper.toYearDateTimestamp(position, startOfDayShift)
                val data = DateSelectorData.Data(
                    topText = "",
                    bottomText = calendar.get(Calendar.YEAR).toString(),
                )
                DateSelectorData.Single(data)
            }
            is RangeLength.All -> {
                val data = DateSelectorData.Data(
                    topText = "",
                    bottomText = resourceRepo.getString(R.string.range_overall),
                )
                DateSelectorData.Wide(data)
            }
            is RangeLength.Last,
            is RangeLength.Custom,
            -> {
                val range = timeMapper.getRangeStartAndEnd(
                    rangeLength = rangeLength,
                    shift = position,
                    firstDayOfWeek = firstDayOfWeek,
                    startOfDayShift = startOfDayShift,
                )
                DateSelectorData.Double(
                    data1 = mapToDateSelectorDayOfMonthData(range.timeStarted),
                    data2 = mapToDateSelectorDayOfMonthData(range.timeEnded - 1),
                )
            }
        }
    }

    fun mapToDateSelectorDayOfMonthData(
        timestamp: Long,
    ): DateSelectorData.Data {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        return DateSelectorData.Data(
            topText = timeMapper.formatShortMonth(calendar.timeInMillis),
            bottomText = calendar.get(Calendar.DAY_OF_MONTH).toString().padDuration(),
        )
    }

    fun mapToSelectRangeName(): String {
        return resourceRepo.getString(R.string.range_custom)
    }

    private fun mapToCustomRangeTitle(range: Range): String {
        // Time ended is the end of selected day, meaning the beginning on the next day.
        return timeMapper.formatDate(range.timeStarted) +
            " - " +
            timeMapper.formatDate(range.timeEnded - 1)
    }

    private fun mapToCustomRangeTitle(
        rangeLength: RangeLength,
        position: Int,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
    ): String {
        val shiftedRange = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = position,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        return mapToCustomRangeTitle(shiftedRange)
    }

    fun mapToLastDaysTitle(days: Int): String {
        return resourceRepo.getQuantityString(R.plurals.range_last, days, days)
    }

    sealed interface DateSelectorData {
        data class Single(val data: Data) : DateSelectorData
        data class Double(val data1: Data, val data2: Data) : DateSelectorData
        data class Wide(val data: Data) : DateSelectorData

        data class Data(
            val topText: String,
            val bottomText: String,
        )
    }
}