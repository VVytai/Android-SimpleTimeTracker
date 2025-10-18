package com.example.util.simpletimetracker.feature_base_adapter.dateSelector

import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_base_adapter.R
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.pxToDp
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClickWith
import com.example.util.simpletimetracker.feature_views.extension.setRounded
import com.example.util.simpletimetracker.feature_base_adapter.InfiniteRecyclerAdapter.Data as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemDateSelectorBinding as Binding

fun createDateSelectorAdapterDelegate(
    mapper: DateSelectorDataMapper,
    onItemClick: ((ViewData) -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        val data = mapper.mapData(item.position)
        val cornerRadius = root.resources.getDimensionPixelSize(R.dimen.record_type_card_corner_radius)
        viewDateSelectorClickable.setRounded(cornerRadius.pxToDp())
        tvDateSelectorDayOfWeek.text = data.dayOfWeek
        tvDateSelectorDayOfMonth.text = data.dayOfMonth
        val textAlpha = if (data.isFuture) 0.4f else 1.0f
        tvDateSelectorDayOfWeek.alpha = textAlpha
        tvDateSelectorDayOfMonth.alpha = textAlpha
        viewDateSelectorBackgroundSelected.isVisible = data.isSelected
        viewDateSelectorBackgroundToday.isVisible = data.isToday
        root.setOnClickWith(item, onItemClick)
    }
}

interface DateSelectorDataMapper {
    fun mapData(position: Int): Data

    data class Data(
        val dayOfWeek: String,
        val dayOfMonth: String,
        val isToday: Boolean,
        val isSelected: Boolean,
        val isFuture: Boolean,
    )
}