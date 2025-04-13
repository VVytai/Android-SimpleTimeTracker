package com.example.util.simpletimetracker.core.dialog

import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams

interface OptionsListDialogListener {

    fun onOptionsItemClick(id: OptionsListParams.Item.Id)
}