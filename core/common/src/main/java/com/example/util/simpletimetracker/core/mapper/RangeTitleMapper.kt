package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.common.R
import com.example.util.simpletimetracker.core.repo.BaseResourceRepo
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
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
            is RangeLength.Last -> mapToLastDaysTitle(rangeLength.days, position, startOfDayShift, firstDayOfWeek)
            is RangeLength.Custom -> if (useShortCustomRange) {
                mapToSelectRangeName()
            } else {
                mapToCustomRangeTitle(rangeLength.range, position, startOfDayShift, firstDayOfWeek)
            }
        }
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
        range: Range,
        position: Int,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
    ): String {
        val shiftedRange = timeMapper.getRangeStartAndEnd(
            rangeLength = RangeLength.Custom(range),
            shift = position,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        return mapToCustomRangeTitle(shiftedRange)
    }

    fun mapToLastDaysTitle(days: Int): String {
        return resourceRepo.getQuantityString(R.plurals.range_last, days, days)
    }

    private fun mapToLastDaysTitle(
        days: Int,
        position: Int,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
    ): String {
        return if (position == 0) {
            mapToLastDaysTitle(days)
        } else {
            timeMapper.getRangeStartAndEnd(
                rangeLength = RangeLength.Last(days),
                shift = position,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift,
            ).let(::mapToCustomRangeTitle)
        }
    }
}