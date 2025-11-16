package com.example.util.simpletimetracker.feature_records.mapper

import com.example.util.simpletimetracker.core.mapper.OptionsListItemMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
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

    suspend fun map(): List<OptionsListParams.Item> {
        val result = mutableListOf<OptionsListParams.Item>()
        val isCalendar = prefsInteractor.getShowRecordsCalendar()
        val filterType = prefsInteractor.getListFilterType()

        result += OptionsListParams.Item(
            id = RecordsContainerOptionsListItem.CalendarView,
            text = if (isCalendar) {
                R.string.records_switch_to_list
            } else {
                R.string.records_switch_to_calendar
            }.let(resourceRepo::getString),
            icon = if (isCalendar) R.drawable.list else R.drawable.calendar,
        )

        result += optionsListItemMapper.mapCommonItem(
            id = RecordsContainerOptionsListItem.Share,
        )

        result += optionsListItemMapper.mapCommonItem(
            id = RecordsContainerOptionsListItem.Filter,
            isIconCheckVisible = optionsListItemMapper.isIconCheckVisible(
                filteredIds = prefsInteractor.getListFilteredIds(filterType),
                existingIds = optionsListItemMapper.getExistingIds(filterType),
            ),
        )

        result += optionsListItemMapper.mapCommonItem(
            id = RecordsContainerOptionsListItem.SelectDate,
        )

        result += optionsListItemMapper.mapCommonItem(
            id = RecordsContainerOptionsListItem.BackToToday,
        )

        return result
    }
}