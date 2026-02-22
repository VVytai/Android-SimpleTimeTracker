package com.example.util.simpletimetracker.domain.statistics.model

data class StatisticsDetailTagValueSettings(
    val tagId: Long,
    val chartValueMode: ChartValueMode,
    val multiplyDuration: Boolean,
    val fillEmptyPeriods: Boolean,
) {

    companion object {
        fun getDefault(tagId: Long = -1L): StatisticsDetailTagValueSettings {
            return StatisticsDetailTagValueSettings(
                tagId = tagId,
                chartValueMode = ChartValueMode.TOTAL,
                multiplyDuration = false,
                fillEmptyPeriods = false,
            )
        }
    }
}