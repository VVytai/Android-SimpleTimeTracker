package com.example.util.simpletimetracker.feature_statistics.mapper

import com.example.util.simpletimetracker.core.mapper.OptionsListItemMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.model.StatisticsContainerOptionsListItem
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class StatisticsContainerOptionsListMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val optionsListItemMapper: OptionsListItemMapper,
) {

    suspend fun map(): List<OptionsListParams.Item> {
        val result = mutableListOf<OptionsListParams.Item>()
        val filterType = prefsInteractor.getChartFilterType()

        result += OptionsListParams.Item(
            id = StatisticsContainerOptionsListItem.Share,
            text = resourceRepo.getString(R.string.message_action_share),
            icon = R.drawable.share,
            isIconCheckVisible = false,
        )

        result += OptionsListParams.Item(
            id = StatisticsContainerOptionsListItem.Filter,
            text = resourceRepo.getString(R.string.chart_filter_hint),
            icon = R.drawable.filter,
            isIconCheckVisible = optionsListItemMapper.isIconCheckVisible(
                filteredIds = prefsInteractor.getChartFilteredIds(filterType),
                existingIds = optionsListItemMapper.getExistingIds(filterType),
            ),
        )

        // TODO DATE select week, select month, select year
        result += OptionsListParams.Item(
            id = StatisticsContainerOptionsListItem.SelectDate,
            text = resourceRepo.getString(R.string.range_select_day),
            icon = R.drawable.date,
            isIconCheckVisible = false,
        )

        result += OptionsListParams.Item(
            id = StatisticsContainerOptionsListItem.BackToToday,
            text = resourceRepo.getString(R.string.range_back_to_today),
            icon = R.drawable.back,
            isIconCheckVisible = false,
        )

        // TODO DATE text and icon
        result += OptionsListParams.Item(
            id = StatisticsContainerOptionsListItem.SelectRange,
            text = "Select range",
            icon = R.drawable.date,
            isIconCheckVisible = false,
        )

        return result
    }
}