package com.example.util.simpletimetracker.feature_base_adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter

class InfiniteRecyclerAdapter(
    val delegate: RecyclerAdapterDelegate,
    diffUtilCallback: DiffUtilCallback = DiffUtilCallback(),
) : ListAdapter<ViewHolderType, BaseRecyclerViewHolder>(diffUtilCallback) {

    var isReady: Boolean = false

    // "Infinite" recycler.
    override fun getItemCount(): Int = if (isReady) Int.MAX_VALUE else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder {
        return delegate.onCreateViewHolder(parent)
    }

    override fun onBindViewHolder(
        holder: BaseRecyclerViewHolder,
        position: Int,
    ) = holder.bind(Data(position.toShift()), emptyList())

    override fun onBindViewHolder(
        holder: BaseRecyclerViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) = holder.bind(Data(position.toShift()), payloads)

    override fun getItemViewType(position: Int): Int = 1

    data class Data(val position: Int) : ViewHolderType {
        override fun getUniqueId(): Long = position.toLong()
        override fun isValidType(other: ViewHolderType): Boolean = other is Data

        // Update always.
        override fun areItemsTheSame(other: ViewHolderType): Boolean = false
        override fun areContentsTheSame(other: ViewHolderType): Boolean = false
    }

    private fun Int.toShift(): Int = this - FIRST

    companion object {
        // First page is at the center of range.
        const val FIRST = Int.MAX_VALUE / 2
    }
}