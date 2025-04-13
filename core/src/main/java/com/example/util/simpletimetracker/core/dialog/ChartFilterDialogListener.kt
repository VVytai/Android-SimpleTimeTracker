package com.example.util.simpletimetracker.core.dialog

import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType

interface ChartFilterDialogListener {

    fun onChartFilterDataSelected(
        chartFilterType: ChartFilterType,
        dataIds: List<Long>,
    )

    fun onChartFilterDialogDismissed()
}