package com.example.util.simpletimetracker.feature_records.model

import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import kotlinx.parcelize.Parcelize

sealed interface RecordsContainerOptionsListItem : OptionsListParams.Item.Id {

    @Parcelize
    data object CalendarView : RecordsContainerOptionsListItem

    @Parcelize
    data object Filter : RecordsContainerOptionsListItem

    @Parcelize
    data object Share : RecordsContainerOptionsListItem

    @Parcelize
    data object Add : RecordsContainerOptionsListItem
}