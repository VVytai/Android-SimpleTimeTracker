package com.example.util.simpletimetracker.feature_statistics_detail.settings.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartValueMode
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.toDomain
import com.example.util.simpletimetracker.feature_statistics_detail.settings.dialog.StatisticsTagValuesSettingsDialogListener
import com.example.util.simpletimetracker.feature_statistics_detail.settings.interactor.StatisticsTagValuesSettingsViewDataInteractor
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsTagValuesSettingsParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsTagValuesSettingsViewModel @Inject constructor(
    private val viewDataInteractor: StatisticsTagValuesSettingsViewDataInteractor,
) : BaseViewModel() {

    val content: LiveData<List<ViewHolderType>> by lazySuspend { loadContent() }
    val settingsChanged: LiveData<StatisticsTagValuesSettingsDialogListener.Result> = SingleLiveEvent()

    private var chartValueMode: ChartValueMode = ChartValueMode.TOTAL
    private var multiplyDuration: Boolean = false
    private var initialized: Boolean = false

    fun initialize(params: StatisticsTagValuesSettingsParams) {
        if (initialized) return
        chartValueMode = params.mode.toDomain()
        multiplyDuration = params.multiplyDuration
        initialized = true
    }

    fun onVisible() = viewModelScope.launch {
        updateContent()
    }

    fun onBlockClicked(block: SettingsBlock) {
        when (block) {
            SettingsBlock.StatisticsTagValuesChartValueMode -> {
                chartValueMode = when (chartValueMode) {
                    ChartValueMode.TOTAL -> ChartValueMode.AVERAGE
                    ChartValueMode.AVERAGE -> ChartValueMode.TOTAL
                }
                onSettingsChanged()
            }
            SettingsBlock.StatisticsTagValuesMultiplyDuration -> {
                multiplyDuration = !multiplyDuration
                onSettingsChanged()
            }
            else -> {
                // Do nothing.
            }
        }
    }

    private fun onSettingsChanged() {
        settingsChanged.set(
            StatisticsTagValuesSettingsDialogListener.Result(
                mode = chartValueMode,
                multiplyDuration = multiplyDuration,
            ),
        )
        updateContent()
    }

    private fun updateContent() {
        content.set(loadContent())
    }

    private fun loadContent(): List<ViewHolderType> {
        return viewDataInteractor.execute(
            chartValueMode = chartValueMode,
            multiplyDuration = multiplyDuration,
        )
    }
}
