package com.example.util.simpletimetracker.core.common.mapper

import com.example.util.simpletimetracker.core.common.R
import com.example.util.simpletimetracker.core.common.repo.BaseResourceRepo
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

class TimeMapper @Inject constructor(
    private val resourceRepo: BaseResourceRepo,
) {

    /**
     * @param forceSeconds - true 1h 7m 21s, false 1h 7m
     * @param useProportionalMinutes - true 1.25h
     */
    fun formatInterval(
        interval: Long,
        forceSeconds: Boolean,
        useProportionalMinutes: Boolean,
    ): String {
        val hourString = resourceRepo.getString(R.string.time_hour)
        val minuteString = resourceRepo.getString(R.string.time_minute)
        val secondString = resourceRepo.getString(R.string.time_second)

        val hr: Long = TimeUnit.MILLISECONDS.toHours(
            abs(interval),
        )
        val min: Long = TimeUnit.MILLISECONDS.toMinutes(
            abs(interval) - TimeUnit.HOURS.toMillis(hr),
        )
        val sec: Long = TimeUnit.MILLISECONDS.toSeconds(
            abs(interval) - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min),
        )

        if (useProportionalMinutes) {
            return formatIntervalProportional(hr, min)
        }

        val willShowHours: Boolean
        val willShowMinutes: Boolean
        val willShowSeconds: Boolean

        if (forceSeconds) {
            willShowHours = hr != 0L
            willShowMinutes = willShowHours || min != 0L
            willShowSeconds = true
        } else {
            willShowHours = hr != 0L
            willShowMinutes = true
            willShowSeconds = false
        }

        var res = ""
        if (willShowHours) res += "$hr$hourString "
        if (willShowMinutes) res += "$min$minuteString"
        if (willShowMinutes && willShowSeconds) res += " "
        if (willShowSeconds) res += "$sec$secondString"

        res = if (interval < 0) "-$res" else res

        return res
    }

    private fun formatIntervalProportional(hr: Long, min: Long): String {
        val hourString = resourceRepo.getString(R.string.time_hour)
        val minutesProportion = min / 60f
        val proportional = hr + minutesProportion
        val proportionalString = "%.2f".format(proportional)

        return "$proportionalString$hourString"
    }
}