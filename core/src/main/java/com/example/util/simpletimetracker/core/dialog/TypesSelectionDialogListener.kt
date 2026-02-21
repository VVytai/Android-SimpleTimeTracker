package com.example.util.simpletimetracker.core.dialog

import com.example.util.simpletimetracker.domain.record.model.RecordBase

interface TypesSelectionDialogListener {

    fun onDataSelected(
        tag: String?,
        dataIds: List<Long>,
        tagValues: List<RecordBase.Tag>,
        selectValueOnStartTagIds: List<Long>,
    )
}