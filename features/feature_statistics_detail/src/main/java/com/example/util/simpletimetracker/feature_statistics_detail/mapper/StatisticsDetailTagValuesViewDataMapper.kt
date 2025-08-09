package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import com.example.util.simpletimetracker.core.extension.removeTrailingZeroes
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.ButtonsRowItemViewData
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailHintViewData
import com.example.util.simpletimetracker.feature_statistics_detail.conts.TAG_VALUE_PRECISION
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataDuration
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartMode
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData
import javax.inject.Inject

class StatisticsDetailTagValuesViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
) {

    fun mapTagValueChartViewData(
        data: List<ChartBarDataDuration>,
        prevData: List<ChartBarDataDuration>,
        rangeLength: RangeLength,
        availableChartGroupings: List<ChartGrouping>,
        appliedChartGrouping: ChartGrouping,
        availableChartLengths: List<ChartLength>,
        appliedChartLength: ChartLength,
        chartMode: ChartMode,
        valueSuffix: String,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        val items = mutableListOf<ViewHolderType>()

        val chartData = statisticsDetailViewDataMapper.mapChartData(
            data = data,
            goal = 0, // Don't show goal.
            rangeLength = rangeLength,
            chartMode = chartMode,
            showSelectedBarOnStart = true,
            useSingleColor = true,
            drawRoundCaps = true,
        )
        val (title, rangeAverages) = statisticsDetailViewDataMapper.getRangeAverages(
            data = data,
            prevData = prevData,
            compareData = emptyList(),
            showComparison = false,
            rangeLength = rangeLength,
            chartGrouping = appliedChartGrouping,
            chartMode = chartMode,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
            isDarkTheme = isDarkTheme,
        )
        val chartGroupingViewData = statisticsDetailViewDataMapper.mapToChartGroupingViewData(
            availableChartGroupings = availableChartGroupings,
            appliedChartGrouping = appliedChartGrouping,
        )
        val chartLengthViewData = statisticsDetailViewDataMapper.mapToChartLengthViewData(
            availableChartLengths = availableChartLengths,
            appliedChartLength = appliedChartLength,
        )
        val totals = mapTagValuesTotals(
            goalData = data,
        )

        if (chartData.visible) {
            val mainHint = resourceRepo.getString(R.string.statistics_detail_tag_values_hint)
            val hint = if (valueSuffix.isEmpty()) {
                mainHint
            } else {
                "$mainHint ($valueSuffix)"
            }
            items += StatisticsDetailHintViewData(
                block = StatisticsDetailBlock.TagValuesHint,
                text = hint,
            )
        }

        if (chartData.visible) {
            items += StatisticsDetailBarChartViewData(
                block = StatisticsDetailBlock.TagValuesChartData,
                singleColor = null,
                marginTopDp = 0,
                data = chartData,
            )
        }

        if (chartGroupingViewData.size > 1) {
            items += ButtonsRowItemViewData(
                block = StatisticsDetailBlock.TagValuesChartGrouping,
                marginTopDp = 4,
                data = chartGroupingViewData,
            )
        }

        if (chartLengthViewData.isNotEmpty()) {
            // Update margin top depending if has buttons before.
            val hasButtonsBefore = items.lastOrNull() is ButtonsRowItemViewData
            val marginTopDp = if (hasButtonsBefore) -10 else 4
            items += ButtonsRowItemViewData(
                block = StatisticsDetailBlock.TagValuesChartLength,
                marginTopDp = marginTopDp,
                data = chartLengthViewData,
            )
        }

        if (rangeAverages.isNotEmpty()) {
            items += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.TagValuesRangeAverages,
                title = title,
                marginTopDp = 0,
                data = rangeAverages,
            )
        }

        if (chartData.visible) {
            items += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.TagValuesTotals,
                title = "",
                marginTopDp = 0,
                data = totals,
            )
        }

        return items
    }

    private fun mapTagValuesTotals(
        goalData: List<ChartBarDataDuration>,
    ): List<StatisticsDetailCardInternalViewData> {
        val emptyValue by lazy { resourceRepo.getString(R.string.statistics_detail_empty) }

        val barValues = goalData.map { bar -> bar.totalDuration }
        val minValue = barValues.minOrNull()?.toFloat()?.div(TAG_VALUE_PRECISION)
        val maxValue = barValues.maxOrNull()?.toFloat()?.div(TAG_VALUE_PRECISION)
        val total = barValues.sum().toFloat() / TAG_VALUE_PRECISION

        return listOf(
            StatisticsDetailCardInternalViewData(
                value = minValue?.toString()?.removeTrailingZeroes() ?: emptyValue,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.records_filter_duration_min),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
            StatisticsDetailCardInternalViewData(
                value = total.toString().removeTrailingZeroes(),
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.statistics_detail_total_duration),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
            StatisticsDetailCardInternalViewData(
                value = maxValue?.toString()?.removeTrailingZeroes() ?: emptyValue,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.records_filter_duration_max),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
        )
    }
}