package com.example.util.simpletimetracker.feature_base_adapter.recordShortcut

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData

data class RecordShortcutViewData(
    val id: Long,
    val record: RecordViewData.Tracked,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is RecordShortcutViewData
}