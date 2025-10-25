package com.example.util.simpletimetracker.feature_base_adapter.dateSelector

import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_base_adapter.InfiniteRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.R
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.pxToDp
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.feature_views.extension.setRounded
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemDateRangeSelectorBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.dateSelector.DateSelectorRangeViewData as ViewData

fun createDateSelectorRangeAdapterDelegate(
    onItemClick: ((ViewData) -> Unit),
    onItemLongClick: ((ViewData) -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        setDayMoth(
            dayMonth = item.dayMonth1,
            day = tvDateSelectorDayOfWeek1,
            month = tvDateSelectorDayOfMonth1,
        )
        setDayMoth(
            dayMonth = item.dayMonth2,
            day = tvDateSelectorDayOfWeek2,
            month = tvDateSelectorDayOfMonth2,
        )
        root.setCardData(
            cardData = item.cardData,
            viewSelected = viewDateSelectorBackgroundSelected,
            viewToday = viewDateSelectorBackgroundToday,
            viewClickable = viewDateSelectorClickable,
            textViews = listOf(
                tvDateSelectorDayOfWeek1,
                tvDateSelectorDayOfMonth1,
                tvDateSelectorDayOfWeek2,
                tvDateSelectorDayOfMonth2,
            ),
        )

        root.setOnClickWith(item, onItemClick)
        root.setOnLongClick { onItemLongClick(item) }
    }
}

data class DateSelectorRangeViewData(
    override val position: Int,
    val dayMonth1: DateSelectorDayViewData.DayMonth,
    val dayMonth2: DateSelectorDayViewData.DayMonth,
    val cardData: DateSelectorDayViewData.CardData,
) : InfiniteRecyclerAdapter.Data {

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}
