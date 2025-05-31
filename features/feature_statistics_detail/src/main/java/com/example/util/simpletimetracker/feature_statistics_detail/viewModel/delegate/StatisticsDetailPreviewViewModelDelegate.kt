package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailPreviewInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreview
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewMoreViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailPreviewViewModelDelegate @Inject constructor(
    private val previewInteractor: StatisticsDetailPreviewInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val viewData: LiveData<StatisticsDetailPreviewCompositeViewData?> by lazySuspend {
        loadViewData().also { parent?.updateContent() }
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null
    private var previewsExpanded: Boolean = false
    private var previewsComparisonExpanded: Boolean = false

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    fun updateViewData() = delegateScope.launch {
        viewData.set(loadViewData())
        parent?.updateContent()
    }

    fun onPreviewItemClick(item: StatisticsDetailPreview) {
        if (item !is StatisticsDetailPreviewMoreViewData) return
        when (item.type) {
            StatisticsDetailPreviewViewData.Type.FILTER -> previewsExpanded = true
            StatisticsDetailPreviewViewData.Type.COMPARISON -> previewsComparisonExpanded = true
        }
        updateViewData()
    }

    private suspend fun loadViewData(): StatisticsDetailPreviewCompositeViewData? {
        val parent = parent ?: return null

        val data = previewInteractor.getPreviewData(
            filterParams = parent.filter,
            isExpanded = previewsExpanded,
            isForComparison = false,
        )
        val comparisonData = previewInteractor.getPreviewData(
            filterParams = parent.comparisonFilter,
            isExpanded = previewsComparisonExpanded,
            isForComparison = true,
        )
        return StatisticsDetailPreviewCompositeViewData(
            data = data.firstOrNull() as? StatisticsDetailPreviewViewData,
            additionalData = data.drop(1),
            comparisonData = comparisonData,
        )
    }
}