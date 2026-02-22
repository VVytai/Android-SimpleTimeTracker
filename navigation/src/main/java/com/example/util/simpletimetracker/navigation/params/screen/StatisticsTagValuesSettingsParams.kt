package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StatisticsTagValuesSettingsParams(
    val mode: Mode,
    val multiplyDuration: Boolean,
) : Parcelable, ScreenParams {

    enum class Mode {
        TOTAL,
        AVERAGE,
    }

    companion object {
        val Empty = StatisticsTagValuesSettingsParams(
            mode = Mode.TOTAL,
            multiplyDuration = false,
        )
    }
}
