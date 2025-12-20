package com.example.util.simpletimetracker.feature_statistics.interactor

import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.interactor.GetStatisticsDetailRangeInteractor
import com.example.util.simpletimetracker.core.interactor.GetTotalStatisticsFilterInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import javax.inject.Inject

class StatisticsDetailTotalNavigator @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val getStatisticsDetailRangeInteractor: GetStatisticsDetailRangeInteractor,
    private val getTotalStatisticsFilterInteractor: GetTotalStatisticsFilterInteractor,
) {

    suspend fun execute(
        shift: Int,
    ) {
        val filter = getTotalStatisticsFilterInteractor.execute(
            filterType = prefsInteractor.getChartFilterType(),
        )

        val params = StatisticsDetailParams(
            transitionName = "",
            filter = filter.let(::listOf).map(RecordsFilter::toParams),
            range = getStatisticsDetailRangeInteractor.execute(),
            shift = shift,
            preview = null,
        )

        router.navigate(params)
    }
}