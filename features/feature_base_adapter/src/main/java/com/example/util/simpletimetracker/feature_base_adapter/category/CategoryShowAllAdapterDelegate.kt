package com.example.util.simpletimetracker.feature_base_adapter.category

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryShowAllViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemCategoryLayoutBinding as Binding

fun createCategoryShowAllAdapterDelegate(
    onItemClick: ((ViewData) -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.viewCategoryItem) {
        item as ViewData

        itemColor = item.color
        itemName = item.name
        itemIconVisible = false
        setOnClickWith(item, onItemClick)
    }
}

class CategoryShowAllViewData(
    val name: String,
    @ColorInt val color: Int,
) : ViewHolderType {

    // Only one item on screen
    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}