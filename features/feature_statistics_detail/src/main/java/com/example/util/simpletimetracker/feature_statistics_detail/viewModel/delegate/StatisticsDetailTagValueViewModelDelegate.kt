package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailTagValueInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailTagValuesCompositeViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailTagValueViewModelDelegate @Inject constructor(
    private val tagValueInteractor: StatisticsDetailTagValueInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val viewData: LiveData<StatisticsDetailTagValuesCompositeViewData> by lazy {
        return@lazy MutableLiveData()
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null
    private var chartGrouping: ChartGrouping = ChartGrouping.DAILY
    private var chartLength: ChartLength = ChartLength.TEN

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    fun onChartGroupingClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailGroupingViewData) return
        this.chartGrouping = viewData.chartGrouping
        updateViewData()
    }

    fun onChartLengthClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailChartLengthViewData) return
        this.chartLength = viewData.chartLength
        updateViewData()
    }

    fun updateViewData() = delegateScope.launch {
        val data = loadViewData() ?: return@launch
        viewData.set(data)
        chartGrouping = data.appliedChartGrouping
        chartLength = data.appliedChartLength
        parent?.updateContent()
    }

    private suspend fun loadViewData(): StatisticsDetailTagValuesCompositeViewData? {
        val parent = parent ?: return null
        return tagValueInteractor.getViewData(
            records = parent.records,
            filter = parent.filter,
            currentChartGrouping = chartGrouping,
            currentChartLength = chartLength,
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
        )
    }
}