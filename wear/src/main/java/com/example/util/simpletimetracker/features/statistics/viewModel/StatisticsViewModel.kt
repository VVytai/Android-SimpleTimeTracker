/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.statistics.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.domain.model.WearChartFilterType
import com.example.util.simpletimetracker.features.statistics.mapper.StatisticsViewDataMapper
import com.example.util.simpletimetracker.features.statistics.screen.StatisticsListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsViewDataMapper: StatisticsViewDataMapper,
    private val wearDataRepo: WearDataRepo,
) : ViewModel() {

    val state: StateFlow<StatisticsListState> get() = _state.asStateFlow()
    private val _state: MutableStateFlow<StatisticsListState> = MutableStateFlow(StatisticsListState.Loading)

    private var isInitialized = false
    private var shift: Int = 0

    fun init() {
        if (isInitialized) return
        viewModelScope.launch { loadData() }
        isInitialized = true
    }

    fun onRefresh() = viewModelScope.launch {
        loadData()
    }

    fun onPrevClick() = viewModelScope.launch {
        shift -= 1
        loadData()
    }

    fun onNextClick() = viewModelScope.launch {
        shift += 1
        loadData()
    }

    private suspend fun loadData() {
        val filterType = WearChartFilterType.ACTIVITY
        val statistics = wearDataRepo.loadStatistics(
            forceReload = true,
            shift = shift,
            filterType = filterType,
        )

        when {
            statistics.isFailure -> {
                showError()
            }
            statistics.getOrNull().isNullOrEmpty() -> {
                _state.value = statisticsViewDataMapper.mapEmptyState()
            }
            else -> {
                _state.value = statisticsViewDataMapper.mapContentState(
                    statistics = statistics.getOrNull().orEmpty(),
                    filterType = filterType,
                )
            }
        }
    }

    private fun showError() {
        _state.value = statisticsViewDataMapper.mapErrorState()
    }
}