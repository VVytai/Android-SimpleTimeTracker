package com.example.util.simpletimetracker.domain.extension

import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag

fun Range?.orEmpty(): Range = this ?: Range(0, 0)

fun List<RecordTag>.getFullName(): String =
    this.joinToString(separator = ", ") { it.name }

fun RecordBase.toRange(): Range {
    return Range(timeStarted = timeStarted, timeEnded = timeEnded)
}