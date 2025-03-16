/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.statistics.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.domain.model.WearActivityIcon
import com.example.util.simpletimetracker.features.statistics.ui.StatisticsButtons
import com.example.util.simpletimetracker.features.statistics.ui.StatisticsChip
import com.example.util.simpletimetracker.features.statistics.ui.StatisticsChipState
import com.example.util.simpletimetracker.features.statistics.ui.StatisticsTitle
import com.example.util.simpletimetracker.presentation.layout.ScaffoldedScrollingColumn
import com.example.util.simpletimetracker.presentation.ui.ErrorState
import com.example.util.simpletimetracker.presentation.ui.RenderLoading
import com.example.util.simpletimetracker.presentation.ui.renderError
import com.example.util.simpletimetracker.utils.getString
import java.util.UUID

sealed interface StatisticsListState {

    data object Loading : StatisticsListState

    data class Error(
        val error: ErrorState,
    ) : StatisticsListState

    data class Empty(
        val title: String,
        @StringRes val messageResId: Int,
    ) : StatisticsListState

    data class Content(
        val title: String,
        val items: List<Item>,
    ) : StatisticsListState {

        sealed interface Item {
            data object Loader : Item
            data class Statistics(val data: StatisticsChipState) : Item
            data class Total(val data: StatisticsChipState) : Item
        }
    }
}

@Composable
fun StatisticsList(
    state: StatisticsListState,
    onRefresh: () -> Unit = {},
    onTitleLongClick: () -> Unit = {},
    onPrevClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
) {
    ScaffoldedScrollingColumn {
        when (state) {
            is StatisticsListState.Loading -> item {
                RenderLoading()
            }
            is StatisticsListState.Error -> {
                renderError(
                    state = state.error,
                    onRefresh = onRefresh,
                )
            }
            is StatisticsListState.Empty -> {
                renderEmptyState(
                    state = state,
                    onTitleLongClick = onTitleLongClick,
                    onPrevClick = onPrevClick,
                    onNextClick = onNextClick,
                )
            }
            is StatisticsListState.Content -> {
                renderContent(
                    state = state,
                    onTitleLongClick = onTitleLongClick,
                    onPrevClick = onPrevClick,
                    onNextClick = onNextClick,
                )
            }
        }
    }
}

private fun ScalingLazyListScope.renderEmptyState(
    state: StatisticsListState.Empty,
    onTitleLongClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    item {
        StatisticsTitle(
            title = state.title,
            onLongClick = onTitleLongClick,
        )
    }
    item {
        StatisticsButtons(
            onPrevClick = onPrevClick,
            onNextClick = onNextClick,
        )
    }
    item {
        Text(
            text = getString(state.messageResId),
            modifier = Modifier.padding(8.dp),
        )
    }
}

private fun ScalingLazyListScope.renderContent(
    state: StatisticsListState.Content,
    onTitleLongClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    item {
        StatisticsTitle(
            title = state.title,
            onLongClick = onTitleLongClick,
        )
    }
    item {
        StatisticsButtons(
            onPrevClick = onPrevClick,
            onNextClick = onNextClick,
        )
    }
    for (itemState in state.items) {
        when (itemState) {
            is StatisticsListState.Content.Item.Loader -> {
                item {
                    RenderLoading()
                }
            }
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
        state = StatisticsListState.Error(
            ErrorState(R.string.wear_loading_error),
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun NoData() {
    StatisticsList(
        state = StatisticsListState.Empty(
            title = "Tue, Mar 12",
            messageResId = R.string.no_data,
        ),
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
            title = "Tue, Mar 12",
            items = items,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun ContentLoading() {
    val items = List(1) {
        StatisticsListState.Content.Item.Loader
    }
    StatisticsList(
        state = StatisticsListState.Content(
            title = "Tue, Mar 12",
            items = items,
        ),
    )
}