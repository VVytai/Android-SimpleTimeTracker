package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OptionsListParams(
    val type: Type,
) : ScreenParams, Parcelable {

    sealed interface Type : Parcelable {

        @Parcelize
        data object RecordsContainer : Type

        @Parcelize
        data object StatisticsContainer : Type

        @Parcelize
        data object StatisticsDetailContainer : Type

        @Parcelize
        data object Categories : Type
    }

    companion object {
        val Empty = OptionsListParams(
            type = Type.RecordsContainer,
        )
    }
}