package com.example.util.simpletimetracker.feature_base_adapter.recordShortcut

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData

data class RecordShortcutViewData(
    val id: Long,
    val hint: String,
    val data: CategoryViewData.Record,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is RecordShortcutViewData
}