/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.statistics.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.presentation.theme.ColorInactive

@Composable
fun StatisticsButtons(
    onPrevClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
) {
    Row {
        StatisticsButton(
            modifier = Modifier.weight(1f),
            iconResId = R.drawable.wear_arrow_left,
            onClick = onPrevClick,
        )
        StatisticsButton(
            modifier = Modifier.weight(1f),
            iconResId = R.drawable.wear_arrow_right,
            onClick = onNextClick,
        )
    }
}

@Composable
private fun StatisticsButton(
    modifier: Modifier,
    @DrawableRes iconResId: Int,
    onClick: () -> Unit = {},
) {
    val shape = RoundedCornerShape(100.dp)
    Icon(
        modifier = modifier
            .background(
                color = ColorInactive,
                shape = shape,
            )
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(4.dp),
        painter = painterResource(iconResId),
        contentDescription = null,
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Preview() {
    StatisticsButtons()
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 2f)
@Composable
private fun PreviewFontScale() {
    StatisticsButtons()
}
