package com.example.util.simpletimetracker

import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_statistics_detail.R as statisticsDetailR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StatisticsDetailTagValueTest : BaseUiTest() {

    @Test
    fun statistics() {
        val name = "TypeName"
        val tag = "TagName"

        // Add activity
        testUtils.addActivity(name)
        testUtils.addRecordTag(tag, name, hasTagValue = true)
        runBlocking { prefsInteractor.setChartFilterType(ChartFilterType.RECORD_TAG) }

        // Add records
        var calendar = Calendar.getInstance()
            .apply { set(Calendar.HOUR_OF_DAY, 15) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1),
            tagNamesWithValues = listOf(tag to 1.5),
        )
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1),
            tagNamesWithValues = listOf(tag to -0.5),
        )
        calendar = Calendar.getInstance()
            .apply { add(Calendar.YEAR, -10) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1),
            tagNamesWithValues = listOf(tag to -0.5),
        )

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(tag), isCompletelyDisplayed())) }
        clickOnViewWithIdOnPager(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_overall)

        // Bar chart
        scrollStatDetailRecyclerToTag(StatisticsDetailBlock.TagValuesChartData)
        checkViewIsDisplayed(
            allOf(withTag(StatisticsDetailBlock.TagValuesChartData), isCompletelyDisplayed()),
        )

        clickOnChartGrouping(coreR.string.statistics_detail_chart_daily)
        clickOnChartLength(coreR.string.statistics_detail_length_ten)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            average = "0.1",
            averageNonEmpty = "1",
        )
        clickOnChartLength(coreR.string.statistics_detail_length_fifty)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            average = "0.02",
            averageNonEmpty = "1",
        )
        clickOnChartLength(coreR.string.statistics_detail_length_hundred)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            average = "0.01",
            averageNonEmpty = "1",
        )

        clickOnChartGrouping(coreR.string.statistics_detail_chart_weekly)
        clickOnChartLength(coreR.string.statistics_detail_length_ten)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_weekly,
            average = "0.1",
            averageNonEmpty = "1",
        )
        clickOnChartLength(coreR.string.statistics_detail_length_fifty)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_weekly,
            average = "0.02",
            averageNonEmpty = "1",
        )
        clickOnChartLength(coreR.string.statistics_detail_length_hundred)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_weekly,
            average = "0.01",
            averageNonEmpty = "1",
        )

        clickOnChartGrouping(coreR.string.statistics_detail_chart_monthly)
        clickOnChartLength(coreR.string.statistics_detail_length_ten)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_monthly,
            average = "0.1",
            averageNonEmpty = "1",
        )
        clickOnChartLength(coreR.string.statistics_detail_length_fifty)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_monthly,
            average = "0.02",
            averageNonEmpty = "1",
        )
        clickOnChartLength(coreR.string.statistics_detail_length_hundred)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_monthly,
            average = "0.01",
            averageNonEmpty = "1",
        )

        clickOnChartGrouping(coreR.string.statistics_detail_chart_yearly)
        clickOnChartLength(coreR.string.statistics_detail_length_ten)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_yearly,
            average = "0.1",
            averageNonEmpty = "1",
        )
        clickOnChartLength(coreR.string.statistics_detail_length_fifty)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_yearly,
            average = "0.01",
            averageNonEmpty = "0.25",
        )
        clickOnChartLength(coreR.string.statistics_detail_length_hundred)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_yearly,
            average = "0.005",
            averageNonEmpty = "0.25",
        )

        // Cards
        clickOnChartLength(coreR.string.statistics_detail_length_ten)
        scrollStatDetailRecyclerToTag(StatisticsDetailBlock.TagValuesTotals)
        checkCard(coreR.string.records_filter_duration_min, "0")
        checkCard(coreR.string.statistics_detail_total_duration, "1")
        checkCard(coreR.string.records_filter_duration_max, "1")

        clickOnChartLength(coreR.string.statistics_detail_length_fifty)
        scrollStatDetailRecyclerToTag(StatisticsDetailBlock.TagValuesTotals)
        checkCard(coreR.string.records_filter_duration_min, "-0.5")
        checkCard(coreR.string.statistics_detail_total_duration, "0.5")
        checkCard(coreR.string.records_filter_duration_max, "1")
    }

    private fun clickOnChartGrouping(withTextId: Int) {
        scrollStatDetailRecyclerToTag(StatisticsDetailBlock.TagValuesChartGrouping)
        clickOnView(
            allOf(
                isDescendantOfA(withTag(StatisticsDetailBlock.TagValuesChartGrouping)),
                withText(withTextId),
            ),
        )
    }

    private fun clickOnChartLength(withTextId: Int) {
        scrollStatDetailRecyclerToTag(StatisticsDetailBlock.TagValuesChartLength)
        clickOnView(
            allOf(
                isDescendantOfA(withTag(StatisticsDetailBlock.TagValuesChartLength)),
                withText(withTextId),
            ),
        )
    }

    private fun BaseUiTest.checkRangeAverages(
        rangeId: Int,
        average: String = "",
        checkAverage: Boolean = true,
        averageNonEmpty: String,
    ) {
        checkRangeAverages(
            block = StatisticsDetailBlock.TagValuesRangeAverages,
            rangeId = rangeId,
            average = average,
            checkAverage = checkAverage,
            averageNonEmpty = averageNonEmpty,
        )
    }
}
