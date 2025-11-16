package com.example.util.simpletimetracker.feature_statistics.mapper

import com.example.util.simpletimetracker.core.mapper.OptionsListItemMapper
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics.model.StatisticsContainerOptionsListItem
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class StatisticsContainerOptionsListMapper @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val optionsListItemMapper: OptionsListItemMapper,
    private val rangeViewDataMapper: RangeViewDataMapper,
) {

    suspend fun map(
        rangeLength: RangeLength,
    ): List<OptionsListParams.Item> {
        val result = mutableListOf<OptionsListParams.Item>()
        val filterType = prefsInteractor.getChartFilterType()

        result += optionsListItemMapper.mapCommonItem(
            id = StatisticsContainerOptionsListItem.Share,
        )

        result += optionsListItemMapper.mapCommonItem(
            id = StatisticsContainerOptionsListItem.Filter,
            isIconCheckVisible = optionsListItemMapper.isIconCheckVisible(
                filteredIds = prefsInteractor.getChartFilteredIds(filterType),
                existingIds = optionsListItemMapper.getExistingIds(filterType),
            ),
        )

        val selectDateName = rangeViewDataMapper.mapToSelectDateName(rangeLength)?.text
        if (selectDateName != null) {
            result += optionsListItemMapper.mapCommonItem(
                id = StatisticsContainerOptionsListItem.SelectDate,
            )?.copy(text = selectDateName)
        }

        result += optionsListItemMapper.mapCommonItem(
            id = StatisticsContainerOptionsListItem.SelectRange,
        )

        // Back to today will not work on overall range because of only one item in the list.
        if (rangeLength != RangeLength.All) {
            result += optionsListItemMapper.mapCommonItem(
                id = StatisticsContainerOptionsListItem.BackToToday,
            )
        }

        return result
    }
}