package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setMargins
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailButtonsRowViewData as ViewData
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailButtonsRowItemBinding as Binding

fun createStatisticsDetailButtonsRowAdapterDelegate(
    onClick: (StatisticsDetailBlock, ButtonsRowViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.root) {
        item as ViewData

        setMargins(top = item.marginTopDp)
        // Replace without animation on reuse for different buttons.
        if (tag != item.block) {
            adapter.replaceFast(item.data)
        } else {
            adapter.replace(item.data)
        }
        listener = { onClick(item.block, it) }
        tag = item.block
    }
}

data class StatisticsDetailButtonsRowViewData(
    val block: StatisticsDetailBlock,
    val marginTopDp: Int,
    val data: List<ViewHolderType>,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}