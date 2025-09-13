package com.example.util.simpletimetracker.feature_base_adapter.recordShortcut

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
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

    with(binding.viewRecordShortcutItem) {
        item as ViewData

        itemColor = item.data.color
        itemName = item.data.name
        itemIconColor = item.data.iconColor
        itemIconAlpha = item.data.iconAlpha
        itemIconVisible = item.data.icon != null
        item.data.icon?.let(this::itemIcon::set)

        setOnClick { onItemClick(item) }
        setOnLongClick { onItemLongClick(item) }
    }
}