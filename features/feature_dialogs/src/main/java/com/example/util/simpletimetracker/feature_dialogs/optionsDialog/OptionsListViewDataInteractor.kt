package com.example.util.simpletimetracker.feature_dialogs.optionsDialog

import com.example.util.simpletimetracker.core.model.OptionsListItem
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.optionsList.OptionsListViewData
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class OptionsListViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
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
        }
    }

    private suspend fun getRecordsContainerViewData(): List<ViewHolderType> {
        return listOfNotNull(
            OptionsListItem.RecordsContainer.CalendarView
                .takeIf { prefsInteractor.getShowCalendarButtonOnRecordsTab() },
            OptionsListItem.RecordsContainer.Share,
            OptionsListItem.RecordsContainer.Filter,
            OptionsListItem.RecordsContainer.Add,
        ).map { mapData(it) }
    }

    private suspend fun getStatisticsContainerViewData(): List<ViewHolderType> {
        return listOfNotNull(
            OptionsListItem.StatisticsContainer.Share,
            OptionsListItem.StatisticsContainer.Filter,
        ).map { mapData(it) }
    }

    private suspend fun getStatisticsDetailContainerViewData(): List<ViewHolderType> {
        return listOfNotNull(
            OptionsListItem.StatisticsDetailContainer.Compare,
            OptionsListItem.StatisticsDetailContainer.Filter,
        ).map { mapData(it) }
    }

    private suspend fun mapData(
        item: OptionsListItem,
    ): OptionsListViewData {
        val text: String
        val iconResId: Int

        when (item) {
            is OptionsListItem.RecordsContainer.CalendarView -> {
                val isCalendar = prefsInteractor.getShowRecordsCalendar()
                iconResId = if (isCalendar) R.drawable.list else R.drawable.calendar
                text = if (isCalendar) {
                    R.string.records_switch_to_list
                } else {
                    R.string.records_switch_to_calendar
                }.let(resourceRepo::getString)
            }
            is OptionsListItem.RecordsContainer.Filter -> {
                text = resourceRepo.getString(R.string.chart_filter_hint)
                iconResId = R.drawable.filter
            }
            is OptionsListItem.RecordsContainer.Share -> {
                text = resourceRepo.getString(R.string.message_action_share)
                iconResId = R.drawable.share
            }
            is OptionsListItem.RecordsContainer.Add -> {
                text = resourceRepo.getString(R.string.records_add_record)
                iconResId = R.drawable.add
            }
            OptionsListItem.StatisticsContainer.Filter -> {
                text = resourceRepo.getString(R.string.chart_filter_hint)
                iconResId = R.drawable.filter
            }
            OptionsListItem.StatisticsContainer.Share -> {
                text = resourceRepo.getString(R.string.message_action_share)
                iconResId = R.drawable.share
            }
            OptionsListItem.StatisticsDetailContainer.Compare -> {
                text = resourceRepo.getString(R.string.types_compare_hint)
                iconResId = R.drawable.compare
            }
            OptionsListItem.StatisticsDetailContainer.Filter -> {
                text = resourceRepo.getString(R.string.chart_filter_hint)
                iconResId = R.drawable.filter
            }
        }

        return OptionsListViewData(
            id = item,
            text = text,
            icon = iconResId,
        )
    }
}