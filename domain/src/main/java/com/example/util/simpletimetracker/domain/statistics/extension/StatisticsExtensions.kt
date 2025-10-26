package com.example.util.simpletimetracker.domain.statistics.extension

import com.example.util.simpletimetracker.domain.statistics.model.RangeLength

// TODO DATE remove
fun RangeLength.canBeSwiped(): Boolean {
    return when (this) {
        is RangeLength.All -> false
        else -> true
    }
}