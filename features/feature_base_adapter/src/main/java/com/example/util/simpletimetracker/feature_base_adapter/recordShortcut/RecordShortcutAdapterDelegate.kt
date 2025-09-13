package com.example.util.simpletimetracker.feature_base_adapter.recordShortcut

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.record.bindState
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemRecordShortcutLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.recordShortcut.RecordShortcutViewData as ViewData

fun createRecordShortcutAdapterDelegate(
    onItemClick: ((ViewData) -> Unit) = { _ -> },
    onItemLongClick: ((ViewData) -> Unit) = { _ -> },
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.viewRecordItem) {
        item as ViewData

        bindState(item.record)
        setOnClick { onItemClick(item) }
        setOnLongClick { onItemLongClick(item) }
    }
}