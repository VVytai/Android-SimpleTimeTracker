package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.utils.LocalDateMapper
import java.time.LocalDate
import java.util.TimeZone
import javax.inject.Inject

class ChangeReminderViewDataMapper @Inject constructor(
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val localDateMapper: LocalDateMapper,
) {

    // TODO show icon - bell crossed, or circle with a minus
    fun mapDndHint(
        doNotDisturbStartMillis: Long,
        doNotDisturbEndMillis: Long,
        useMilitaryTime: Boolean,
    ): String? {
        if (doNotDisturbStartMillis == doNotDisturbEndMillis) return null

        fun formatTime(
            timeOfDayMillis: Long,
        ): String {
            val timeZone = TimeZone.getDefault()
            return formatTimeOfDay(
                millis = timeOfDayMillis,
                useMilitaryTime = useMilitaryTime,
                date = LocalDate.now(timeZone.toZoneId()),
                timeZone = timeZone,
            )
        }

        return listOf(
            formatTime(doNotDisturbStartMillis),
            formatTime(doNotDisturbEndMillis),
        ).joinToString(separator = "-")
    }

    fun formatTimeOfDay(
        millis: Long,
        useMilitaryTime: Boolean,
        date: LocalDate,
        timeZone: TimeZone,
    ): String {
        val timestamp = localDateMapper.resolveDateTime(
            date = date,
            timeOfDayMillis = millis,
            timeZone = timeZone,
        ) ?: return resourceRepo.getString(R.string.no_data)
        return timeMapper.formatTime(
            time = timestamp,
            useMilitaryTime = useMilitaryTime,
            showSeconds = false,
        )
    }
}