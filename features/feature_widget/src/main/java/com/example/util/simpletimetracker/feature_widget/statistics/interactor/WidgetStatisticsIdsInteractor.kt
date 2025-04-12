package com.example.util.simpletimetracker.feature_widget.statistics.interactor

import com.example.util.simpletimetracker.domain.base.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.statistics.model.StatisticsWidgetData
import javax.inject.Inject

class WidgetStatisticsIdsInteractor @Inject constructor() {

    fun getAllTypeIds(
        typeIds: Set<Long>,
    ): Set<Long> {
        return typeIds + UNTRACKED_ITEM_ID
    }

    fun getAllCategoryIds(
        categoryIds: Set<Long>,
    ): Set<Long> {
        return categoryIds + UNTRACKED_ITEM_ID + UNCATEGORIZED_ITEM_ID
    }

    fun getAllTagIds(
        tagIds: Set<Long>,
    ): Set<Long> {
        return tagIds + UNTRACKED_ITEM_ID + UNCATEGORIZED_ITEM_ID
    }

    suspend fun getActualFilteredIds(
        widgetData: StatisticsWidgetData,
        typeIds: suspend () -> Set<Long>,
        categoryIds: suspend () -> Set<Long>,
        tagIds: suspend () -> Set<Long>,
    ): List<Long> {
        val filterType = widgetData.chartFilterType

        val widgetItemIds = when (filterType) {
            ChartFilterType.ACTIVITY -> widgetData.typeIds
            ChartFilterType.CATEGORY -> widgetData.categoryIds
            ChartFilterType.RECORD_TAG -> widgetData.tagIds
        }.toList()

        return when (widgetData.filteringType) {
            StatisticsWidgetData.FilterType.FILTER -> {
                widgetItemIds
            }
            StatisticsWidgetData.FilterType.SELECT -> {
                val allIds = when (filterType) {
                    ChartFilterType.ACTIVITY -> getAllTypeIds(typeIds.invoke())
                    ChartFilterType.CATEGORY -> getAllCategoryIds(categoryIds.invoke())
                    ChartFilterType.RECORD_TAG -> getAllTagIds(tagIds.invoke())
                }
                allIds.filter { it !in widgetItemIds }
            }
        }
    }
}