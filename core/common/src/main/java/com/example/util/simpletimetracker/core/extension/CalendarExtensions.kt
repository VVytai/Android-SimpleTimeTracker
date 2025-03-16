package com.example.util.simpletimetracker.core.extension

import java.util.Calendar

fun Calendar.setToStartOfDay() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

fun Calendar.setWeekToFirstDay() {
    val another = Calendar.getInstance()
    another.timeInMillis = timeInMillis

    val currentTime = another.timeInMillis
    // Setting DAY_OF_WEEK have a weird behaviour so as if after that another field is set -
    // it would reset to current day. Use another calendar to manipulate day of week and get its time.
    another.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    // If went to future - go back a week
    if (another.timeInMillis > currentTime) {
        another.add(Calendar.DATE, -7)
    }

    timeInMillis = another.timeInMillis
}

fun Calendar.shift(shift: Long): Calendar {
    // Example
    // shift 6h
    // before 2023-03-26T00:00+01:00[Europe/Amsterdam] DST_OFFSET = 0
    // after 2023-03-26T07:00+02:00[Europe/Amsterdam] DST_OFFSET = 3600000
    // need to compensate one hour.
    val dstOffsetBefore = get(Calendar.DST_OFFSET)
    timeInMillis += shift
    val dstOffsetAfter = get(Calendar.DST_OFFSET)
    timeInMillis += (dstOffsetBefore - dstOffsetAfter)
    return this
}

fun Calendar.shiftTimeStamp(timestamp: Long, shift: Long): Long {
    timeInMillis = timestamp
    shift(shift)
    return timeInMillis
}