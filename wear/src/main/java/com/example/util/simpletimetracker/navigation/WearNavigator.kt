/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.navigation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.util.simpletimetracker.features.activities.screen.ActivitiesScreen
import com.example.util.simpletimetracker.features.settings.screen.SettingsScreen
import com.example.util.simpletimetracker.features.statistics.screen.StatisticsScreen
import com.example.util.simpletimetracker.features.tagsSelection.screen.TagsScreen
import com.example.util.simpletimetracker.presentation.datePicker.WearDatePicker
import com.example.util.simpletimetracker.presentation.dialog.MessageDialog
import com.example.util.simpletimetracker.utils.getString
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

object Route {
    const val ACTIVITIES = "activities"
    const val TAGS = "activities/{id}/tags"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val ALERT = "alert/{textResId}"
    const val DATE_PICKER = "datePicker/{timestamp}"
}

@Composable
fun WearNavigator() {
    val navigation = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(
        navController = navigation,
        startDestination = Route.ACTIVITIES,
    ) {
        composable(Route.ACTIVITIES) {
            ActivitiesScreen(
                onRequestTagSelection = {
                    val route = Route.TAGS.replace("{id}", it.toString())
                    navigation.navigate(route)
                },
                onStatisticsClick = {
                    navigation.navigate(Route.STATISTICS)
                },
                onSettingsClick = {
                    navigation.navigate(Route.SETTINGS)
                },
                onShowMessage = {
                    val route = Route.ALERT.replace("{textResId}", it.toString())
                    navigation.navigate(route)
                },
            )
        }
        composable(Route.TAGS) {
            val activityId = it.arguments
                ?.getString("id")
                ?.toLong()
                ?: return@composable

            TagsScreen(
                activityId = activityId,
                onComplete = {
                    navigation.popBackStack()
                },
            )
        }
        composable(Route.STATISTICS) {
            StatisticsScreen(
                onOpenDatePicker = {
                    val route = Route.DATE_PICKER.replace("{timestamp}", it.toString())
                    navigation.navigate(route)
                },
            )
        }
        composable(Route.SETTINGS) {
            SettingsScreen()
        }
        composable(Route.ALERT) {
            val textResId = it.arguments
                ?.getString("textResId")
                ?.toIntOrNull()
                ?: return@composable

            MessageDialog(
                message = getString(textResId),
                onDismiss = {
                    navigation.popBackStack()
                },
            )
        }
        composable(Route.DATE_PICKER) {
            val timestamp = it.arguments
                ?.getString("timestamp")
                ?.toLong()
                ?: return@composable
            val instant = Instant.ofEpochMilli(timestamp)
            val date = LocalDate.ofInstant(instant, ZoneOffset.UTC)

            WearDatePicker(
                date = date,
                onComplete = {
                    navigation.popBackStack()
                },
            )
        }
    }
}
