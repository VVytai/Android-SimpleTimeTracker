/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.statistics.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.simpletimetracker.features.statistics.viewModel.StatisticsViewModel

@Composable
fun StatisticsScreen() {
    val viewModel = hiltViewModel<StatisticsViewModel>()
    viewModel.init()
    val state by viewModel.state.collectAsStateWithLifecycle()

    StatisticsList(
        state = state,
        onRefresh = viewModel::onRefresh,
        onTitleLongClick = viewModel::onTitleLongClick,
        onPrevClick = viewModel::onPrevClick,
        onNextClick = viewModel::onNextClick,
    )
}
