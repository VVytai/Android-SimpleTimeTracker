/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.domain.model.WearActivityIcon
import com.example.util.simpletimetracker.presentation.ui.layout.ScaffoldedScrollingColumn
import com.example.util.simpletimetracker.utils.getString
import java.util.UUID

sealed interface StatisticsListState {
    data object Loading : StatisticsListState

    data class Error(
        @StringRes val messageResId: Int,
    ) : StatisticsListState

    data class Empty(
        @StringRes val messageResId: Int,
    ) : StatisticsListState

    data class Content(
        val items: List<Item>,
    ) : StatisticsListState {

        sealed interface Item {
            data class Statistics(val data: StatisticsChipState) : Item
            data class Total(val data: StatisticsChipState) : Item
        }
    }
}

@Composable
fun StatisticsList(
    state: StatisticsListState,
    onRefresh: () -> Unit = {},
) {
    ScaffoldedScrollingColumn(
        startItemIndex = 0,
    ) {
        when (state) {
            is StatisticsListState.Loading -> item {
                RenderLoading()
            }
            is StatisticsListState.Error -> item {
                RenderError(state, onRefresh)
            }
            is StatisticsListState.Empty -> item {
                RenderEmptyState(state)
            }
            is StatisticsListState.Content -> {
                renderContent(state)
            }
        }
    }
}

// TODO move to outer element, replace in other screens.
// TODO same for error.
@Composable
private fun RenderLoading() {
    CircularProgressIndicator(
        modifier = Modifier.width(64.dp),
    )
}

@Composable
private fun RenderError(
    state: StatisticsListState.Error,
    onRefresh: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.wear_connection_error),
            contentDescription = null,
        )
        Text(
            text = getString(stringResId = state.messageResId),
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center,
        )
        RefreshButton(onRefresh)
    }
}

@Composable
private fun RenderEmptyState(
    state: StatisticsListState.Empty,
) {
    Text(
        text = getString(state.messageResId),
        modifier = Modifier.padding(8.dp),
    )
}

private fun ScalingLazyListScope.renderContent(
    state: StatisticsListState.Content,
) {
    for (itemState in state.items) {
        when (itemState) {
            is StatisticsListState.Content.Item.Statistics -> {
                item(key = itemState.data.id) {
                    StatisticsChip(itemState.data)
                }
            }
            is StatisticsListState.Content.Item.Total -> {
                item(key = itemState.data.name) {
                    StatisticsChip(itemState.data)
                }
            }
        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Loading() {
    StatisticsList(
        state = StatisticsListState.Loading,
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Error() {
    StatisticsList(
        state = StatisticsListState.Error(R.string.wear_loading_error),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun NoData() {
    StatisticsList(
        state = StatisticsListState.Empty(R.string.no_data),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Content() {
    val items = List(5) {
        StatisticsChipState(
            id = UUID.randomUUID().hashCode().toLong(),
            name = "Sleep",
            icon = WearActivityIcon.Image(R.drawable.ic_hotel_24px),
            color = 0xFF0000FA,
            duration = "10h 8m 20s",
            percent = "$it%",
        ).let {
            StatisticsListState.Content.Item.Statistics(it)
        }
    }
    StatisticsList(
        state = StatisticsListState.Content(
            items = items,
        ),
    )
}