package com.example.util.simpletimetracker.feature_statistics.adapter

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsViewData
import com.example.util.simpletimetracker.feature_base_adapter.statistics.bind
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemStatisticsLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsInfoViewData as ViewData

fun createStatisticsInfoAdapterDelegate(
    onItemClick: ((StatisticsViewData, Map<Any, String>) -> Unit)?,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        viewStatisticsItem.bind(
            item = item.data,
            onItemClick = onItemClick,
        )
    }
}

data class StatisticsInfoViewData(
    val data: StatisticsViewData,
) : ViewHolderType {

    override fun getUniqueId(): Long = data.name.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}