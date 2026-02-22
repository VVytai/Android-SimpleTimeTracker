package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartValueMode
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsTagValuesSettingsParams

fun ChartValueMode.toParams(): StatisticsTagValuesSettingsParams.Mode {
    return when (this) {
        ChartValueMode.TOTAL -> StatisticsTagValuesSettingsParams.Mode.TOTAL
        ChartValueMode.AVERAGE -> StatisticsTagValuesSettingsParams.Mode.AVERAGE
    }
}

fun StatisticsTagValuesSettingsParams.Mode.toDomain(): ChartValueMode {
    return when (this) {
        StatisticsTagValuesSettingsParams.Mode.TOTAL -> ChartValueMode.TOTAL
        StatisticsTagValuesSettingsParams.Mode.AVERAGE -> ChartValueMode.AVERAGE
    }
}
