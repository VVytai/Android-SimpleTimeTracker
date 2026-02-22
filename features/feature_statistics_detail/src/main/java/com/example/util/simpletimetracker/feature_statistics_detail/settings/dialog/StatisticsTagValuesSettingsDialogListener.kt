package com.example.util.simpletimetracker.feature_statistics_detail.settings.dialog

import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartValueMode

interface StatisticsTagValuesSettingsDialogListener {

    fun onStatisticsTagValuesSettingsChanged(result: Result)

    data class Result(
        val mode: ChartValueMode,
        val multiplyDuration: Boolean,
    )
}
