package com.example.util.simpletimetracker.core.dialog

import com.example.util.simpletimetracker.navigation.params.screen.ArchiveDialogParams

// TODO move to dialog api module
interface ArchiveDialogListener {

    fun onDeleteClick(params: ArchiveDialogParams)
    fun onRestoreClick(params: ArchiveDialogParams)
}