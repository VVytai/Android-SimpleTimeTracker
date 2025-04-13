package com.example.util.simpletimetracker.feature_dialogs.optionsDialog

import com.example.util.simpletimetracker.core.model.OptionsListItem
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.optionsList.OptionsListViewData
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class OptionsListViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
) {

    suspend fun getViewData(
        extra: OptionsListParams,
    ): List<ViewHolderType> {
        return when (extra.type) {
            is OptionsListParams.Type.RecordsContainer -> {
                getRecordsContainerViewData()
            }
            is OptionsListParams.Type.StatisticsContainer -> {
                getStatisticsContainerViewData()
            }
            is OptionsListParams.Type.StatisticsDetailContainer -> {
                getStatisticsDetailContainerViewData()
            }
            is OptionsListParams.Type.Categories -> {
                getCategoriesViewData()
            }
        }
    }

    private suspend fun getRecordsContainerViewData(): List<ViewHolderType> {
        return listOfNotNull(
            OptionsListItem.RecordsContainer.CalendarView
                .takeIf { prefsInteractor.getShowCalendarButtonOnRecordsTab() },
            OptionsListItem.RecordsContainer.Share,
            OptionsListItem.RecordsContainer.Filter,
            OptionsListItem.RecordsContainer.Add,
        ).let { mapData(it) }
    }

    private suspend fun getStatisticsContainerViewData(): List<ViewHolderType> {
        return listOfNotNull(
            OptionsListItem.StatisticsContainer.Share,
            OptionsListItem.StatisticsContainer.Filter,
        ).let { mapData(it) }
    }

    private suspend fun getStatisticsDetailContainerViewData(): List<ViewHolderType> {
        return listOfNotNull(
            OptionsListItem.StatisticsDetailContainer.Compare,
            OptionsListItem.StatisticsDetailContainer.Filter,
        ).let { mapData(it) }
    }

    private suspend fun getCategoriesViewData(): List<ViewHolderType> {
        return listOfNotNull(
            OptionsListItem.Categories.EnabledSearch,
            OptionsListItem.Categories.Filter,
        ).let { mapData(it) }
    }

    private suspend fun mapData(
        items: List<OptionsListItem>,
    ): List<OptionsListViewData> {
        return items.map {
            mapData(
                item = it,
                getExistingIds = { filterType ->
                    // Provide lazily.
                    getExistingIds(filterType)
                },
            )
        }
    }

    // TODO FILTER refactor
    private suspend fun mapData(
        item: OptionsListItem,
        getExistingIds: suspend (ChartFilterType) -> Map<Long, Boolean>,
    ): OptionsListViewData {
        val text: String
        val iconResId: Int
        val isIconCheckVisible: Boolean

        when (item) {
            is OptionsListItem.RecordsContainer.CalendarView -> {
                val isCalendar = prefsInteractor.getShowRecordsCalendar()
                iconResId = if (isCalendar) R.drawable.list else R.drawable.calendar
                text = if (isCalendar) {
                    R.string.records_switch_to_list
                } else {
                    R.string.records_switch_to_calendar
                }.let(resourceRepo::getString)
                isIconCheckVisible = false
            }
            is OptionsListItem.RecordsContainer.Filter -> {
                text = resourceRepo.getString(R.string.chart_filter_hint)
                iconResId = R.drawable.filter
                val filterType = prefsInteractor.getListFilterType()
                isIconCheckVisible = isIconCheckVisible(
                    filteredIds = prefsInteractor.getListFilteredIds(filterType),
                    existingIds = getExistingIds(filterType),
                )
            }
            is OptionsListItem.RecordsContainer.Share -> {
                text = resourceRepo.getString(R.string.message_action_share)
                iconResId = R.drawable.share
                isIconCheckVisible = false
            }
            is OptionsListItem.RecordsContainer.Add -> {
                text = resourceRepo.getString(R.string.records_add_record)
                iconResId = R.drawable.add
                isIconCheckVisible = false
            }
            OptionsListItem.StatisticsContainer.Filter -> {
                text = resourceRepo.getString(R.string.chart_filter_hint)
                iconResId = R.drawable.filter
                val filterType = prefsInteractor.getChartFilterType()
                isIconCheckVisible = isIconCheckVisible(
                    filteredIds = prefsInteractor.getChartFilteredIds(filterType),
                    existingIds = getExistingIds(filterType),
                )
            }
            OptionsListItem.StatisticsContainer.Share -> {
                text = resourceRepo.getString(R.string.message_action_share)
                iconResId = R.drawable.share
                isIconCheckVisible = false
            }
            OptionsListItem.StatisticsDetailContainer.Compare -> {
                text = resourceRepo.getString(R.string.types_compare_hint)
                iconResId = R.drawable.compare
                isIconCheckVisible = false
            }
            OptionsListItem.StatisticsDetailContainer.Filter -> {
                text = resourceRepo.getString(R.string.chart_filter_hint)
                iconResId = R.drawable.filter
                isIconCheckVisible = false
            }
            OptionsListItem.Categories.Filter -> {
                text = resourceRepo.getString(R.string.chart_filter_hint)
                iconResId = R.drawable.filter
                isIconCheckVisible = false // TODO FILTER get from repo
            }
            OptionsListItem.Categories.EnabledSearch -> {
                text = resourceRepo.getString(R.string.enable_search_hint)
                iconResId = R.drawable.search
                isIconCheckVisible = prefsInteractor.getIsCategoriesSearchEnabled()
            }
        }

        return OptionsListViewData(
            id = item,
            text = text,
            icon = iconResId,
            isIconCheckVisible = isIconCheckVisible,
        )
    }

    private fun isIconCheckVisible(
        filteredIds: List<Long>,
        existingIds: Map<Long, Boolean>,
    ): Boolean {
        return filteredIds.any {
            val isSpecial = it in listOf(UNTRACKED_ITEM_ID, UNCATEGORIZED_ITEM_ID)
            val notRemoved = it in existingIds
            !isSpecial && notRemoved
        }
    }

    // Map is faster for contains.
    private suspend fun getExistingIds(
        chartFilterType: ChartFilterType,
    ): Map<Long, Boolean> {
        return when (chartFilterType) {
            ChartFilterType.ACTIVITY -> recordTypeInteractor.getAll().map { it.id }
            ChartFilterType.CATEGORY -> categoryInteractor.getAll().map { it.id }
            ChartFilterType.RECORD_TAG -> recordTagInteractor.getAll().map { it.id }
        }.associateWith { true }
    }
}