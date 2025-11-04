package com.example.util.simpletimetracker.feature_base_adapter.dateSelector

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_base_adapter.InfiniteRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.R
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
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
            topText = tvDateSelectorTopText,
            bottomText = tvDateSelectorBottomText,
        )
        root.setCardData(
            cardData = item.cardData,
            viewSelected = viewDateSelectorBackgroundSelected,
            viewToday = viewDateSelectorBackgroundToday,
            viewClickable = viewDateSelectorClickable,
            textViews = listOf(
                tvDateSelectorTopText,
                tvDateSelectorBottomText,
            ),
        )

        root.setOnClickWith(item, onItemClick)
        root.setOnLongClick { onItemLongClick(item) }
    }
}

internal fun setDayMoth(
    dayMonth: ViewData.DayMonth,
    topText: TextView,
    bottomText: TextView,
) {
    topText.text = dayMonth.topText
    topText.isVisible = dayMonth.topText.isNotEmpty()
    bottomText.text = dayMonth.bottomText
}

internal fun View.setCardData(
    cardData: ViewData.CardData,
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
        val topText: String,
        val bottomText: String,
    )

    data class CardData(
        val isToday: Boolean,
        val isSelected: Boolean,
        val isFuture: Boolean,
    )

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}
