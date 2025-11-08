package com.example.util.simpletimetracker.feature_base_adapter.optionsList

import android.view.View
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.tryCast
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.extension.safeUpdateLayoutParams
import com.example.util.simpletimetracker.feature_views.extension.setMargins
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_views.extension.setRounded
import com.example.util.simpletimetracker.feature_views.extension.setRoundedEnd
import com.example.util.simpletimetracker.feature_views.extension.setRoundedStart
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemOptionsListLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.optionsList.OptionsListViewData as ViewData

fun createOptionsListAdapterDelegate(
    onClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        root.post {
            val layoutParams = root.layoutParams as? GridLayoutManager.LayoutParams
            val columnIndex = layoutParams?.spanIndex.orZero()
            val spanSize = layoutParams?.spanSize.orZero()

            if (item.isSelected) {
                val selectedRadius = 8
                viewItemOptionsListSelectedBackground.apply {
                    when {
                        item.isFullWidth -> setRounded(0)
                        columnIndex > 0 -> setRoundedStart(selectedRadius)
                        else -> setRoundedEnd(selectedRadius)
                    }
                }
            }

            // Divider is at start of each element, so look for prev item.
            val prevSelected = getAdjacentItems(
                root = root,
                item = item,
                columnIndex = columnIndex,
                spanSize = spanSize,
            ).first?.tryCast<ViewData>()?.isSelected.orFalse()

            viewItemOptionsListVerticalDivider.isVisible =
                !item.isFullWidth && columnIndex != 0 && !item.isSelected && !prevSelected
        }

        tvItemOptionsList.text = item.text
        val textMargin = if (item.icon != null || item.isChecked) 50 else 4
        tvItemOptionsList.setMargins(start = textMargin, end = textMargin)

        if (item.icon != null) {
            ivItemOptionsListIcon.setImageResource(item.icon)
            cardItemOptionsListIcon.isVisible = true
        } else {
            cardItemOptionsListIcon.isVisible = false
        }
        viewItemOptionsListCheckmark.itemCheckState = if (item.isIconCheckVisible) {
            GoalCheckmarkView.CheckState.GOAL_NOT_REACHED
        } else {
            GoalCheckmarkView.CheckState.HIDDEN
        }
        ivItemOptionsListCheck.isVisible = item.isChecked
        viewItemOptionsListSelectedBackground.isVisible = item.isSelected

        root.setOnClickWith(item, onClick)
    }
}

private fun getAdjacentItems(
    root: View,
    item: ViewHolderType,
    columnIndex: Int,
    spanSize: Int,
): Pair<ViewHolderType?, ViewHolderType?> {
    val recycler = root.parent as? RecyclerView
    val adapter = recycler?.adapter as? BaseRecyclerAdapter
    val layoutManager = recycler?.layoutManager as? GridLayoutManager

    val spanCount = layoutManager?.spanCount?.takeIf { it > 0 } ?: return null to null
    if (spanSize == 0 || spanCount == spanSize) return null to null // item is full width.
    val currentPosition = adapter?.currentList?.indexOf(item).takeIf { it != -1 }.orZero()
    val rowStartPosition = currentPosition - columnIndex
    val rowEndPosition = rowStartPosition + spanCount - 1

    fun getItem(position: Int?): ViewHolderType? {
        return adapter?.currentList?.getOrNull(position ?: return null)
    }

    val prev = (currentPosition - 1).takeIf { it >= rowStartPosition }
    val next =  (currentPosition + 1).takeIf { it <= rowEndPosition }

    return getItem(prev) to getItem(next)
}

data class OptionsListViewData(
    val id: Id,
    val text: String,
    @DrawableRes val icon: Int?,
    val isIconCheckVisible: Boolean,
    val isChecked: Boolean,
    val isSelected: Boolean,
    val isFullWidth: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = id.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ViewData

    interface Id
}