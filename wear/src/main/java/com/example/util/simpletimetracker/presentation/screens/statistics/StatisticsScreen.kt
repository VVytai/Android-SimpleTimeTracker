/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.statistics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.simpletimetracker.presentation.ui.components.StatisticsList

@Composable
fun StatisticsScreen() {
    val viewModel = hiltViewModel<StatisticsViewModel>()
    viewModel.init()
    val state by viewModel.state.collectAsStateWithLifecycle()

    StatisticsList(
        state = state,
        onRefresh = viewModel::onRefresh,
    )
}
