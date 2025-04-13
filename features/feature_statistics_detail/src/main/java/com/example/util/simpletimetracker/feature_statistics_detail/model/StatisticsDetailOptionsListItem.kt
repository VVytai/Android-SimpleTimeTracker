package com.example.util.simpletimetracker.feature_statistics_detail.model

import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import kotlinx.parcelize.Parcelize

sealed interface StatisticsDetailOptionsListItem : OptionsListParams.Item.Id {

    @Parcelize
    data object Filter : StatisticsDetailOptionsListItem

    @Parcelize
    data object Compare : StatisticsDetailOptionsListItem
}