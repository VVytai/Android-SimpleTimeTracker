/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.statistics

import androidx.compose.ui.graphics.toArgb
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.core.common.mapper.StatisticsMapper
import com.example.util.simpletimetracker.core.common.mapper.TimeMapper
import com.example.util.simpletimetracker.data.WearIconMapper
import com.example.util.simpletimetracker.data.WearResourceRepo
import com.example.util.simpletimetracker.domain.base.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.model.WearActivityIcon
import com.example.util.simpletimetracker.domain.model.WearChartFilterType
import com.example.util.simpletimetracker.domain.model.WearStatistics
import com.example.util.simpletimetracker.presentation.theme.ColorInactive
import com.example.util.simpletimetracker.presentation.ui.components.StatisticsChipState
import com.example.util.simpletimetracker.presentation.ui.components.StatisticsListState
import javax.inject.Inject

class StatisticsViewDataMapper @Inject constructor(
    private val wearIconMapper: WearIconMapper,
    private val resourceRepo: WearResourceRepo,
    private val timeMapper: TimeMapper,
    private val statisticsMapper: StatisticsMapper,
) {

    // TODO remove
    // TODO move StatisticsList to separate package, split other classes to packages
    fun mapErrorState(): StatisticsListState.Error {
        return StatisticsListState.Error(R.string.wear_loading_error)
    }

    fun mapEmptyState(): StatisticsListState.Empty {
        return StatisticsListState.Empty(R.string.no_data)
    }

    fun mapContentState(
        statistics: List<WearStatistics>,
        filterType: WearChartFilterType,
    ): StatisticsListState.Content {
        val items = mutableListOf<StatisticsListState.Content.Item>()

        val sumDuration = statistics.sumOf { it.duration }
        val statisticsSize = statistics.size

        items += statistics
            .mapNotNull { statistic ->
                val item = mapItem(
                    filterType = filterType,
                    statistics = statistic,
                    sumDuration = sumDuration,
                    statisticsSize = statisticsSize,
                ) ?: return@mapNotNull null
                item to statistic.duration
            }
            .sortedByDescending { (_, duration) -> duration }
            .map { (statistics, _) ->
                StatisticsListState.Content.Item.Statistics(statistics)
            }

        items += mapTotalItem(
            total = getStatisticsTotalTracked(statistics),
        ).let {
            StatisticsListState.Content.Item.Total(it)
        }

        return StatisticsListState.Content(
            items = items,
        )
    }

    private fun mapItem(
        filterType: WearChartFilterType,
        statistics: WearStatistics,
        sumDuration: Long,
        statisticsSize: Int,
    ): StatisticsChipState? {
        val durationPercent = statisticsMapper.getDurationPercentString(
            sumDuration = sumDuration,
            duration = statistics.duration,
            statisticsSize = statisticsSize,
        )
        val duration = timeMapper.formatInterval(
            interval = statistics.duration,
            forceSeconds = true,
            useProportionalMinutes = false,
        )

        return when {
            statistics.id == UNTRACKED_ITEM_ID -> {
                StatisticsChipState(
                    id = statistics.id,
                    name = R.string.untracked_time_name
                        .let(resourceRepo::getString),
                    icon = WearActivityIcon.Image(R.drawable.wear_unknown),
                    color = ColorInactive.toArgb().toLong(),
                    duration = duration,
                    percent = durationPercent,
                )
            }
            statistics.id == UNCATEGORIZED_ITEM_ID -> {
                StatisticsChipState(
                    id = statistics.id,
                    name = if (filterType == WearChartFilterType.RECORD_TAG) {
                        R.string.change_record_untagged
                    } else {
                        R.string.uncategorized_time_name
                    }.let(resourceRepo::getString),
                    icon = WearActivityIcon.Image(R.drawable.wear_untagged),
                    color = ColorInactive.toArgb().toLong(),
                    duration = duration,
                    percent = durationPercent,
                )
            }
            statistics.name != null &&
                statistics.icon != null &&
                statistics.color != null -> {
                StatisticsChipState(
                    id = statistics.id,
                    name = statistics.name,
                    icon = wearIconMapper.mapIcon(statistics.icon),
                    color = statistics.color,
                    duration = duration,
                    percent = durationPercent,
                )
            }
            else -> null
        }
    }

    private fun mapTotalItem(
        total: String,
    ): StatisticsChipState {
        return StatisticsChipState(
            id = 0,
            name = resourceRepo.getString(R.string.statistics_total_tracked),
            icon = null,
            color = ColorInactive.toArgb().toLong(),
            duration = total,
            percent = null,
        )
    }

    private fun getStatisticsTotalTracked(
        statistics: List<WearStatistics>,
    ): String {
        val statisticsFiltered = statistics
            .filterNot { it.id == UNTRACKED_ITEM_ID }
        val total = statisticsFiltered.sumOf { it.duration }
        return timeMapper.formatInterval(
            interval = total,
            forceSeconds = true,
            useProportionalMinutes = false,
        )
    }
}