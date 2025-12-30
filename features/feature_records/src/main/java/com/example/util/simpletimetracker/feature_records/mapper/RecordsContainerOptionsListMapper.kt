package com.example.util.simpletimetracker.feature_records.mapper

import com.example.util.simpletimetracker.core.mapper.OptionsListItemMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.CommonOptionsListItem
import com.example.util.simpletimetracker.domain.base.ContainerOptionsModel
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_records.model.RecordsContainerOptionsListItem
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class RecordsContainerOptionsListMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val optionsListItemMapper: OptionsListItemMapper,
) {

    suspend fun map(
        filterHidden: Boolean,
    ): List<OptionsListParams.Item> {
        val result = mutableListOf<OptionsListParams.Item>()

        val isCalendar = prefsInteractor.getShowRecordsCalendar()
        val hiddenContainerOptions = prefsInteractor.getHiddenContainerOptions()
            .filterIsInstance<ContainerOptionsModel.Records>()
            .map(::mapItemFromModel)

        result += listOf(
            RecordsContainerOptionsListItem.CalendarView,
            RecordsContainerOptionsListItem.Share,
            RecordsContainerOptionsListItem.Filter,
            RecordsContainerOptionsListItem.SelectDate,
            RecordsContainerOptionsListItem.BackToToday,
        ).filter {
            !filterHidden || it !in hiddenContainerOptions
        }.mapNotNull { id ->
            when (id) {
                is RecordsContainerOptionsListItem.Share -> mapCommonItem(id)
                is RecordsContainerOptionsListItem.SelectDate -> mapCommonItem(id)
                is RecordsContainerOptionsListItem.BackToToday -> mapCommonItem(id)
                is RecordsContainerOptionsListItem.Filter -> mapCommonItem(id)
                is RecordsContainerOptionsListItem.CalendarView,
                -> OptionsListParams.Item(
                    id = RecordsContainerOptionsListItem.CalendarView,
                    text = if (isCalendar) {
                        R.string.records_switch_to_list
                    } else {
                        R.string.records_switch_to_calendar
                    }.let(resourceRepo::getString),
                    icon = if (isCalendar) R.drawable.list else R.drawable.calendar,
                )
            }
        }

        return result
    }

    fun mapItemFromModel(model: ContainerOptionsModel.Records): RecordsContainerOptionsListItem {
        return when (model) {
            is ContainerOptionsModel.Records.CalendarView -> RecordsContainerOptionsListItem.CalendarView
            is ContainerOptionsModel.Records.Filter -> RecordsContainerOptionsListItem.Filter
            is ContainerOptionsModel.Records.Share -> RecordsContainerOptionsListItem.Share
            is ContainerOptionsModel.Records.BackToToday -> RecordsContainerOptionsListItem.BackToToday
            is ContainerOptionsModel.Records.SelectDate -> RecordsContainerOptionsListItem.SelectDate
        }
    }

    fun mapItemToModel(id: RecordsContainerOptionsListItem): ContainerOptionsModel {
        return when (id) {
            is RecordsContainerOptionsListItem.CalendarView -> ContainerOptionsModel.Records.CalendarView
            is RecordsContainerOptionsListItem.Filter -> ContainerOptionsModel.Records.Filter
            is RecordsContainerOptionsListItem.Share -> ContainerOptionsModel.Records.Share
            is RecordsContainerOptionsListItem.BackToToday -> ContainerOptionsModel.Records.BackToToday
            is RecordsContainerOptionsListItem.SelectDate -> ContainerOptionsModel.Records.SelectDate
        }
    }

    suspend fun <T> mapCommonItem(id: T): OptionsListParams.Item?
        where T : CommonOptionsListItem, T : OptionsListParams.Item.Id {
        return optionsListItemMapper.mapCommonItem(
            id = id,
            isIconCheckVisible = if (id is RecordsContainerOptionsListItem.Filter) {
                val filterType = prefsInteractor.getListFilterType()
                optionsListItemMapper.isIconCheckVisible(
                    filteredIds = prefsInteractor.getListFilteredIds(filterType),
                    existingIds = optionsListItemMapper.getExistingIds(filterType),
                )
            } else {
                false
            },
        )
    }
}