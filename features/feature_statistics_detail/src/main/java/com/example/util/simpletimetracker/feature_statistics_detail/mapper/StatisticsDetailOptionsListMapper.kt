package com.example.util.simpletimetracker.feature_statistics_detail.mapper

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
) {

    fun map(
        rangeLength: RangeLength,
    ): List<OptionsListParams.Item> {
        val result = mutableListOf<OptionsListParams.Item>()

        result += OptionsListParams.Item(
            id = StatisticsDetailOptionsListItem.Compare,
            text = resourceRepo.getString(R.string.types_compare_hint),
            icon = R.drawable.compare,
            isIconCheckVisible = false,
        )

        result += OptionsListParams.Item(
            id = StatisticsDetailOptionsListItem.Filter,
            text = resourceRepo.getString(R.string.chart_filter_hint),
            icon = R.drawable.filter,
            isIconCheckVisible = false,
        )

        val selectDateName = rangeViewDataMapper.mapToSelectDateName(rangeLength)?.text
        if (selectDateName != null) {
            result += OptionsListParams.Item(
                id = StatisticsDetailOptionsListItem.SelectDate,
                text = selectDateName,
                icon = R.drawable.date,
                isIconCheckVisible = false,
            )
        }

        result += OptionsListParams.Item(
            id = StatisticsDetailOptionsListItem.SelectRange,
            text = resourceRepo.getString(R.string.range_select_range),
            icon = R.drawable.range,
            isIconCheckVisible = false,
        )

        // Back to today will not work on overall range because of only one item in the list.
        if (rangeLength != RangeLength.All) {
            result += OptionsListParams.Item(
                id = StatisticsDetailOptionsListItem.BackToToday,
                text = resourceRepo.getString(R.string.range_back_to_today),
                icon = R.drawable.back,
                isIconCheckVisible = false,
            )
        }

        return result
    }
}