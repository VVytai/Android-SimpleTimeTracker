package com.example.util.simpletimetracker.feature_base_adapter.dateSelector

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_base_adapter.InfiniteRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.R
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.dateSelector.DateSelectorDayViewData
import com.example.util.simpletimetracker.feature_views.extension.pxToDp
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.feature_views.extension.setRounded
import com.example.util.simpletimetracker.feature_base_adapter.dateSelector.DateSelectorDayViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemDateDaySelectorBinding as Binding

fun createDateSelectorDayAdapterDelegate(
    onItemClick: ((ViewData) -> Unit),
    onItemLongClick: ((ViewData) -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        setDayMoth(
            dayMonth = item.dayMonth,
            day = tvDateSelectorDayOfWeek,
            month = tvDateSelectorDayOfMonth,
        )
        root.setCardData(
            cardData = item.cardData,
            viewSelected = viewDateSelectorBackgroundSelected,
            viewToday = viewDateSelectorBackgroundToday,
            viewClickable = viewDateSelectorClickable,
            textViews = listOf(
                tvDateSelectorDayOfWeek,
                tvDateSelectorDayOfMonth,
            ),
        )

        root.setOnClickWith(item, onItemClick)
        root.setOnLongClick { onItemLongClick(item) }
    }
}

internal fun setDayMoth(
    dayMonth: DateSelectorDayViewData.DayMonth,
    day: TextView,
    month: TextView,
) {
    day.text = dayMonth.dayOfWeek
    month.text = dayMonth.dayOfMonth
}

internal fun View.setCardData(
    cardData: DateSelectorDayViewData.CardData,
    viewSelected: View,
    viewToday: View,
    viewClickable: View,
    textViews: List<View>,
) {
    val cornerRadius = resources.getDimensionPixelSize(R.dimen.record_type_card_corner_radius)
    viewClickable.setRounded(cornerRadius.pxToDp())

    val textAlpha = if (cardData.isFuture) 0.4f else 1.0f
    textViews.forEach { it.alpha = textAlpha }

    viewSelected.isVisible = cardData.isSelected
    viewToday.isVisible = cardData.isToday
}

data class DateSelectorDayViewData(
    override val position: Int,
    val dayMonth: DayMonth,
    val cardData: CardData,
) : InfiniteRecyclerAdapter.Data {

    data class DayMonth(
        val dayOfWeek: String,
        val dayOfMonth: String,
    )

    data class CardData(
        val isToday: Boolean,
        val isSelected: Boolean,
        val isFuture: Boolean,
    )

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}
