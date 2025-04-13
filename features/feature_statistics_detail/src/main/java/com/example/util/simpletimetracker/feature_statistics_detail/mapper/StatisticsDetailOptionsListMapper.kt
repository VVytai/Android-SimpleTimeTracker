package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.model.StatisticsDetailOptionsListItem
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class StatisticsDetailOptionsListMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun map(): List<OptionsListParams.Item> {
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

        return result
    }
}