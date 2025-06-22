package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.statistics.model.StatisticsStreaksType

data class StatisticsDetailStreaksTypeViewData(
    val type: StatisticsStreaksType,
    override val name: String,
    override val isSelected: Boolean,
) : ButtonsRowViewData() {

    override val id: Long = type.ordinal.toLong()
}