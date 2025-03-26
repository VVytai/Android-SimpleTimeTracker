package com.example.util.simpletimetracker.feature_base_adapter.optionsList

import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemOptionsListLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.optionsList.OptionsListViewData as ViewData

fun createOptionsListAdapterDelegate(
    onClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvItemOptionsList.text = item.text
        ivItemOptionsListIcon.setImageResource(item.icon)
        root.setOnClickWith(item, onClick)
    }
}

data class OptionsListViewData(
    val id: Id,
    val text: String,
    @DrawableRes val icon: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = id.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ViewData

    interface Id
}