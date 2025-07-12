package com.example.util.simpletimetracker.core.dialog

import com.example.util.simpletimetracker.navigation.params.screen.RecordTagValueSelectionParams

interface OnTagValueSelectedListener {

    fun onTagValueSelected(
        params: RecordTagValueSelectionParams,
        data: Double,
    )
}