package com.example.util.simpletimetracker.feature_reminders.mapper

import com.example.util.simpletimetracker.core.mapper.ChangeReminderViewDataMapper
import java.time.LocalDate
import java.util.TimeZone
import javax.inject.Inject

class RemindersCommonViewDataMapper @Inject constructor(
    private val changeReminderViewDataMapper: ChangeReminderViewDataMapper,
) {

    // TODO don't show if disabled, return null
    // TODO show icon - bell crossed, or circle with a minus
    fun mapDndHint(
        doNotDisturbStartMillis: Long,
        doNotDisturbEndMillis: Long,
        useMilitaryTime: Boolean,
    ): String {
        return listOf(
            formatTime(doNotDisturbStartMillis, useMilitaryTime),
            formatTime(doNotDisturbEndMillis, useMilitaryTime),
        ).joinToString(separator = "-")
    }

    private fun formatTime(
        timeOfDayMillis: Long,
        useMilitaryTime: Boolean,
    ): String {
        val timeZone = TimeZone.getDefault()
        return changeReminderViewDataMapper.formatTimeOfDay(
            millis = timeOfDayMillis,
            useMilitaryTime = useMilitaryTime,
            date = LocalDate.now(timeZone.toZoneId()),
            timeZone = timeZone,
        )
    }
}