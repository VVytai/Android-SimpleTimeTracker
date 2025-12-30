package com.example.util.simpletimetracker.feature_settings.customizeOptionsMenu

import com.example.util.simpletimetracker.domain.base.ContainerOptionsModel
import com.example.util.simpletimetracker.feature_records.mapper.RecordsContainerOptionsListMapper
import com.example.util.simpletimetracker.feature_records.model.RecordsContainerOptionsListItem
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_statistics.mapper.StatisticsContainerOptionsListMapper
import com.example.util.simpletimetracker.feature_statistics.model.StatisticsContainerOptionsListItem
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class CustomizeOptionsMenuMapper @Inject constructor(
    private val recordsContainerOptionsListMapper: RecordsContainerOptionsListMapper,
    private val statisticsContainerOptionsListMapper: StatisticsContainerOptionsListMapper,
) {

    fun mapBlockFromModel(model: ContainerOptionsModel): SettingsBlock {
        return when (model) {
            is ContainerOptionsModel.Records.CalendarView -> SettingsBlock.CustomizeOptionsMenuRecordsCalendar
            is ContainerOptionsModel.Records.Filter -> SettingsBlock.CustomizeOptionsMenuRecordsFilter
            is ContainerOptionsModel.Records.Share -> SettingsBlock.CustomizeOptionsMenuRecordsShare
            is ContainerOptionsModel.Records.BackToToday -> SettingsBlock.CustomizeOptionsMenuRecordsBackToToday
            is ContainerOptionsModel.Records.SelectDate -> SettingsBlock.CustomizeOptionsMenuRecordsSelectDate
            is ContainerOptionsModel.Statistics.Filter -> SettingsBlock.CustomizeOptionsMenuStatisticsFilter
            is ContainerOptionsModel.Statistics.Share -> SettingsBlock.CustomizeOptionsMenuStatisticsShare
            is ContainerOptionsModel.Statistics.BackToToday -> SettingsBlock.CustomizeOptionsMenuStatisticsBackToToday
            is ContainerOptionsModel.Statistics.SelectDate -> SettingsBlock.CustomizeOptionsMenuStatisticsSelectDate
            is ContainerOptionsModel.Statistics.SelectRange -> SettingsBlock.CustomizeOptionsMenuStatisticsSelectRange
        }
    }

    fun mapBlockToModel(block: SettingsBlock): ContainerOptionsModel? {
        return when (block) {
            SettingsBlock.CustomizeOptionsMenuRecordsCalendar -> ContainerOptionsModel.Records.CalendarView
            SettingsBlock.CustomizeOptionsMenuRecordsFilter -> ContainerOptionsModel.Records.Filter
            SettingsBlock.CustomizeOptionsMenuRecordsShare -> ContainerOptionsModel.Records.Share
            SettingsBlock.CustomizeOptionsMenuRecordsBackToToday -> ContainerOptionsModel.Records.BackToToday
            SettingsBlock.CustomizeOptionsMenuRecordsSelectDate -> ContainerOptionsModel.Records.SelectDate
            SettingsBlock.CustomizeOptionsMenuStatisticsFilter -> ContainerOptionsModel.Statistics.Filter
            SettingsBlock.CustomizeOptionsMenuStatisticsShare -> ContainerOptionsModel.Statistics.Share
            SettingsBlock.CustomizeOptionsMenuStatisticsBackToToday -> ContainerOptionsModel.Statistics.BackToToday
            SettingsBlock.CustomizeOptionsMenuStatisticsSelectDate -> ContainerOptionsModel.Statistics.SelectDate
            SettingsBlock.CustomizeOptionsMenuStatisticsSelectRange -> ContainerOptionsModel.Statistics.SelectRange
            else -> null
        }
    }

    fun mapItemFromModel(model: ContainerOptionsModel): OptionsListParams.Item.Id {
        return when (model) {
            is ContainerOptionsModel.Records -> recordsContainerOptionsListMapper.mapItemFromModel(model)
            is ContainerOptionsModel.Statistics -> statisticsContainerOptionsListMapper.mapItemFromModel(model)
        }
    }

    fun mapItemToModel(id: OptionsListParams.Item.Id): ContainerOptionsModel? {
        return when (id) {
            is RecordsContainerOptionsListItem -> recordsContainerOptionsListMapper.mapItemToModel(id)
            is StatisticsContainerOptionsListItem -> statisticsContainerOptionsListMapper.mapItemToModel(id)
            else -> null
        }
    }
}