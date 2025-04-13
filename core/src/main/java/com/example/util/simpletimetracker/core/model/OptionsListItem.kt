package com.example.util.simpletimetracker.core.model

import com.example.util.simpletimetracker.feature_base_adapter.optionsList.OptionsListViewData

sealed interface OptionsListItem : OptionsListViewData.Id {

    sealed interface RecordsContainer : OptionsListItem {
        data object CalendarView : RecordsContainer
        data object Filter : RecordsContainer
        data object Share : RecordsContainer
        data object Add : RecordsContainer
    }

    sealed interface StatisticsContainer : OptionsListItem {
        data object Filter : StatisticsContainer
        data object Share : StatisticsContainer
    }

    sealed interface StatisticsDetailContainer : OptionsListItem {
        data object Filter : StatisticsDetailContainer
        data object Compare : StatisticsDetailContainer
    }

    sealed interface Categories : OptionsListItem {
        data object Filter : Categories
        data object EnabledSearch : Categories
    }
}