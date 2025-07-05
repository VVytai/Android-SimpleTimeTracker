package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.activityFilter.model.ActivityFilter
import com.example.util.simpletimetracker.domain.activityFilter.model.ActivityFilterType
import com.example.util.simpletimetracker.domain.activityFilter.model.PredefinedFilter
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterAddViewData
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData
import javax.inject.Inject

class ActivityFilterViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
) {

    fun mapFiltered(
        filter: ActivityFilter,
        isDarkTheme: Boolean,
        selected: Boolean,
    ): ActivityFilterViewData {
        return ActivityFilterViewData(
            id = filter.id,
            name = filter.name,
            iconColor = mapFilterIconColor(
                selected = selected,
                isDarkTheme = isDarkTheme,
            ),
            color = mapFilterColor(
                selected = selected,
                color = filter.color,
                isDarkTheme = isDarkTheme,
            ),
            selected = selected,
            type = ActivityFilterType.Default,
        )
    }

    fun mapFiltered(
        filter: PredefinedFilter,
        isDarkTheme: Boolean,
        selected: Boolean,
    ): ActivityFilterViewData {
        return ActivityFilterViewData(
            id = filter.categoryId,
            name = filter.name,
            iconColor = mapFilterIconColor(
                selected = selected,
                isDarkTheme = isDarkTheme,
            ),
            color = mapFilterColor(
                selected = selected,
                color = filter.color,
                isDarkTheme = isDarkTheme,
            ),
            selected = selected,
            type = ActivityFilterType.Predefined,
        )
    }

    fun mapToActivityFilterAddItem(
        isDarkTheme: Boolean,
    ): ActivityFilterAddViewData {
        return ActivityFilterAddViewData(
            type = ActivityFilterAddViewData.Type.ADD,
            name = resourceRepo.getString(R.string.running_records_add_filter),
            color = colorMapper.toInactiveColor(isDarkTheme),
        )
    }

    fun mapToActivityFilterToggleItem(
        isFiltersCollapsed: Boolean,
        isDarkTheme: Boolean,
    ): ActivityFilterAddViewData {
        val nameResId = if (isFiltersCollapsed) {
            R.string.show
        } else {
            R.string.hide
        }
        return ActivityFilterAddViewData(
            type = ActivityFilterAddViewData.Type.TOGGLE_VISIBILITY,
            name = resourceRepo.getString(nameResId),
            color = colorMapper.toInactiveColor(isDarkTheme),
        )
    }

    private fun mapFilterIconColor(
        selected: Boolean,
        isDarkTheme: Boolean,
    ): Int {
        return if (selected) {
            colorMapper.toIconColor(isDarkTheme)
        } else {
            colorMapper.toFilteredIconColor(isDarkTheme)
        }
    }

    private fun mapFilterColor(
        selected: Boolean,
        color: AppColor,
        isDarkTheme: Boolean,
    ): Int {
        return if (selected) {
            colorMapper.mapToColorInt(color, isDarkTheme)
        } else {
            colorMapper.toFilteredColor(isDarkTheme)
        }
    }
}