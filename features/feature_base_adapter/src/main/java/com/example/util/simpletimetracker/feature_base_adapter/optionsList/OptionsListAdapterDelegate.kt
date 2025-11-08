package com.example.util.simpletimetracker.feature_base_adapter.optionsList

import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.extension.safeUpdateLayoutParams
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
            val columnIndex = (root.layoutParams as? GridLayoutManager.LayoutParams)?.spanIndex.orZero()

            if (item.isChecked) {
                val checkMarkBias = if (columnIndex > 0) 1f else 0f
                ivItemOptionsListCheck.safeUpdateLayoutParams<ConstraintLayout.LayoutParams> {
                    horizontalBias = checkMarkBias
                }
            }

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
        }

        tvItemOptionsList.text = item.text

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