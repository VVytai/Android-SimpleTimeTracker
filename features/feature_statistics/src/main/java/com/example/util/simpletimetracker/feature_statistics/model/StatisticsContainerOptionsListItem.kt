package com.example.util.simpletimetracker.feature_statistics.model

import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import kotlinx.parcelize.Parcelize

sealed interface StatisticsContainerOptionsListItem : OptionsListParams.Item.Id {

    @Parcelize
    data object Filter : StatisticsContainerOptionsListItem

    @Parcelize
    data object Share : StatisticsContainerOptionsListItem
}