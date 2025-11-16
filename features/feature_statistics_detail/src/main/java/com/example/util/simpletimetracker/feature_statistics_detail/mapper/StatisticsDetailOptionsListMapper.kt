package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import com.example.util.simpletimetracker.core.mapper.OptionsListItemMapper
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.model.StatisticsDetailOptionsListItem
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class StatisticsDetailOptionsListMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val rangeViewDataMapper: RangeViewDataMapper,
    private val optionsListItemMapper: OptionsListItemMapper,
) {

    fun map(
        rangeLength: RangeLength,
    ): List<OptionsListParams.Item> {
        val result = mutableListOf<OptionsListParams.Item>()

        result += OptionsListParams.Item(
            id = StatisticsDetailOptionsListItem.Compare,
            text = resourceRepo.getString(R.string.types_compare_hint),
            icon = R.drawable.compare,
        )

        result += optionsListItemMapper.mapCommonItem(
            id = StatisticsDetailOptionsListItem.Filter,
        )

        val selectDateName = rangeViewDataMapper.mapToSelectDateName(rangeLength)?.text
        if (selectDateName != null) {
            result += optionsListItemMapper.mapCommonItem(
                id = StatisticsDetailOptionsListItem.SelectDate,
            )?.copy(text = selectDateName)
        }

        result += optionsListItemMapper.mapCommonItem(
            id = StatisticsDetailOptionsListItem.SelectRange,
        )

        // Back to today will not work on overall range because of only one item in the list.
        if (rangeLength != RangeLength.All) {
            result += optionsListItemMapper.mapCommonItem(
                id = StatisticsDetailOptionsListItem.BackToToday,
            )
        }

        return result
    }
}