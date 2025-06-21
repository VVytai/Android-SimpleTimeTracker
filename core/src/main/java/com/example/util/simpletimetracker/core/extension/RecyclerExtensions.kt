package com.example.util.simpletimetracker.core.extension

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import java.util.Collections
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_views.extension.dpToPx

fun RecyclerView.onItemMoved(
    getIsSelectable: (RecyclerView.ViewHolder?) -> Boolean = { true },
    getSelectablePositions: ((RecyclerView.ViewHolder?) -> Pair<Int, Int>)? = null,
    onSelected: (RecyclerView.ViewHolder?) -> Unit = {},
    onClear: (RecyclerView.ViewHolder) -> Unit = {},
    onMoved: (items: List<ViewHolderType>, from: Int, to: Int) -> Unit = { _, _, _ -> },
) {
    val dragDirections = ItemTouchHelper.DOWN or ItemTouchHelper.UP or
        ItemTouchHelper.START or ItemTouchHelper.END

    fun getNewItems(
        adapter: BaseRecyclerAdapter,
        fromPosition: Int,
        toPosition: Int,
    ): List<ViewHolderType> {
        val newList = adapter.currentList.toList()

        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(newList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(newList, i, i - 1)
            }
        }

        return newList
    }

    val helper = object : ItemTouchHelper.SimpleCallback(0, 0) {

        override fun getDragDirs(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
        ): Int {
            return if (getIsSelectable(viewHolder)) dragDirections else 0
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder,
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            getSelectablePositions?.invoke(viewHolder)?.let { (start, end) ->
                if (toPosition < start) return false
                if (toPosition > end) return false
            }

            (adapter as? BaseRecyclerAdapter)?.let { adapter ->
                val newItems = getNewItems(adapter, fromPosition, toPosition)
                adapter.submitList(newItems)
                onMoved(newItems, fromPosition, toPosition)
            }

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // Do nothing
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) onSelected(viewHolder)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            onClear(viewHolder)
        }
    }

    (ItemTouchHelper(helper)).attachToRecyclerView(this)
}

fun RecyclerView.onItemSwiped(
    @DrawableRes iconRes: Int,
    @ColorInt iconColor: Int,
    @ColorInt backgroundColor: Int,
    getIsSelectable: (RecyclerView.ViewHolder?) -> Boolean = { true },
    onSwiped: (RecyclerView.ViewHolder?) -> Unit,
) {
    val swipeDirections = ItemTouchHelper.START
    val deleteIcon by lazy {
        ContextCompat.getDrawable(context, iconRes)?.mutate()?.apply { setTint(iconColor) }
    }
    val intrinsicWidth = deleteIcon?.intrinsicWidth.orZero()
    val intrinsicHeight = deleteIcon?.intrinsicHeight.orZero()
    val background = ColorDrawable()
    val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    val helper = object : ItemTouchHelper.SimpleCallback(0, 0) {

        override fun getSwipeDirs(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
        ): Int {
            return if (getIsSelectable(viewHolder)) swipeDirections else 0
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder,
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            onSwiped(viewHolder)
        }

        override fun onChildDraw(
            canvas: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean,
        ) {
            val itemView = viewHolder.itemView
            val isCanceled = dX == 0f

            if (isCanceled) {
                canvas.drawRect(
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat(),
                    clearPaint,
                )
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, false)
                return
            }

            // Draw the red delete background
            background.color = backgroundColor
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom,
            )
            background.draw(canvas)

            // Calculate position of delete icon
            val itemHeight = itemView.bottom - itemView.top
            val iconMargin = 16.dpToPx()
            val iconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
            val iconLeft = itemView.right - iconMargin - intrinsicWidth
            val iconRight = itemView.right - iconMargin
            val iconBottom = iconTop + intrinsicHeight

            // Draw the delete icon
            deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            deleteIcon?.draw(canvas)

            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    (ItemTouchHelper(helper)).attachToRecyclerView(this)
}