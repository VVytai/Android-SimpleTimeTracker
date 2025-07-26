package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTagValueType
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailPreviewInteractor.PreviewType
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartMode
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartSplitSortMode
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailTagValuesCompositeViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsDetailTagValueInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val chartInteractor: StatisticsDetailChartInteractor,
    private val statisticsDetailPreviewInteractor: StatisticsDetailPreviewInteractor,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
) {

    // TODO compare?
    suspend fun getViewData(
        records: List<RecordBase>,
        filter: List<RecordsFilter>,
        currentChartGrouping: ChartGrouping,
        currentChartLength: ChartLength,
        rangeLength: RangeLength,
        rangePosition: Int,
    ): StatisticsDetailTagValuesCompositeViewData = withContext(Dispatchers.Default) {
        val tags = recordTagInteractor.getAll()
        val needToShowTagValue = needToShowTagValue(filter, tags)

        if (!needToShowTagValue) {
            return@withContext StatisticsDetailTagValuesCompositeViewData(
                viewData = emptyList(),
                appliedChartGrouping = currentChartGrouping,
                appliedChartLength = currentChartLength,
            )
        }

        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val useMonthDayTimeFormat = prefsInteractor.getUseMonthDayTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val types = recordTypeInteractor.getAll()
        val typesMap = types.associateBy(RecordType::id)
        val typesOrder = types.map(RecordType::id)
        val chartMode = ChartMode.TAG_VALUE

        val compositeData = chartInteractor.getChartRangeSelectionData(
            currentChartGrouping = currentChartGrouping,
            currentChartLength = currentChartLength,
            rangeLength = rangeLength,
            firstDayOfWeek = firstDayOfWeek,
        )
        val ranges = chartInteractor.getRanges(
            compositeData = compositeData,
            rangeLength = rangeLength,
            rangePosition = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
            useMonthDayTimeFormat = useMonthDayTimeFormat,
        )
        val data = chartInteractor.getChartData(
            allRecords = records,
            ranges = ranges,
            typesOrder = typesOrder,
            typesMap = typesMap,
            isDarkTheme = isDarkTheme,
            chartMode = chartMode,
            splitByActivity = false,
            splitSortMode = ChartSplitSortMode.ACTIVITY_ORDER,
        )
        val prevData = chartInteractor.getPrevData(
            rangeLength = rangeLength,
            compositeData = compositeData,
            rangePosition = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
            useMonthDayTimeFormat = useMonthDayTimeFormat,
            records = records,
            typesOrder = typesOrder,
            typesMap = typesMap,
            isDarkTheme = isDarkTheme,
            chartMode = chartMode,
            splitSortMode = ChartSplitSortMode.ACTIVITY_ORDER,
        )

        val chartViewData = statisticsDetailViewDataMapper.mapTagValueChartViewData(
            data = data,
            prevData = prevData,
            rangeLength = rangeLength,
            availableChartGroupings = compositeData.availableChartGroupings,
            appliedChartGrouping = compositeData.appliedChartGrouping,
            availableChartLengths = compositeData.availableChartLengths,
            appliedChartLength = compositeData.appliedChartLength,
            chartMode = chartMode,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
            isDarkTheme = isDarkTheme,
        )

        return@withContext StatisticsDetailTagValuesCompositeViewData(
            viewData = chartViewData,
            appliedChartGrouping = compositeData.appliedChartGrouping,
            appliedChartLength = compositeData.appliedChartLength,
        )
    }

    private fun needToShowTagValue(
        filter: List<RecordsFilter>,
        tags: List<RecordTag>,
    ): Boolean {
        val previewType = statisticsDetailPreviewInteractor.getPreviewType(filter)
        val selectedTags = filter.getSelectedTags().filterIsInstance<RecordsFilter.TagItem.Tagged>()
        val selectedTag = selectedTags.firstOrNull()
        val tagType = tags.firstOrNull { it.id == selectedTag?.tagId }?.valueType

        return previewType is PreviewType.SelectedTags &&
            selectedTags.size == 1 &&
            tagType == RecordTagValueType.NUMERIC
    }
}