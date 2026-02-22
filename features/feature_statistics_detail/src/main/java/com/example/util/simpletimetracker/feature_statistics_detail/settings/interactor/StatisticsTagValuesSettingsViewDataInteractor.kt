package com.example.util.simpletimetracker.feature_statistics_detail.settings.interactor

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.views.SettingsCheckboxViewData
import com.example.util.simpletimetracker.feature_settings.views.SettingsSelectorViewData
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartValueMode
import javax.inject.Inject

class StatisticsTagValuesSettingsViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun execute(
        chartValueMode: ChartValueMode,
        multiplyDuration: Boolean,
    ): List<ViewHolderType> {
        return listOf(
            SettingsSelectorViewData(
                block = SettingsBlock.StatisticsTagValuesChartValueMode,
                title = resourceRepo.getString(R.string.statistics_detail_tag_values_hint),
                subtitle = "",
                selectedValue = mapChartValueMode(chartValueMode),
                backgroundIsVisible = false,
            ),
            SettingsCheckboxViewData(
                block = SettingsBlock.StatisticsTagValuesMultiplyDuration,
                title = resourceRepo.getString(R.string.statistics_detail_tag_values_multiply_duration),
                subtitle = "",
                isChecked = multiplyDuration,
                dividerIsVisible = false,
                backgroundIsVisible = false,
            ),
        )
    }

    private fun mapChartValueMode(mode: ChartValueMode): String {
        return when (mode) {
            ChartValueMode.TOTAL ->
                resourceRepo.getString(R.string.statistics_detail_total_duration)
            ChartValueMode.AVERAGE ->
                resourceRepo.getString(R.string.statistics_detail_average_record)
        }
    }
}
