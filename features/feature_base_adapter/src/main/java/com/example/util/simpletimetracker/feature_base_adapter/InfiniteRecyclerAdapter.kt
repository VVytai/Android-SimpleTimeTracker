package com.example.util.simpletimetracker.feature_base_adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter

class InfiniteRecyclerAdapter(
    private val dataProvider: DataProvider,
    vararg delegatesList: RecyclerAdapterDelegate,
    diffUtilCallback: DiffUtilCallback = DiffUtilCallback(),
) : ListAdapter<ViewHolderType, BaseRecyclerViewHolder>(diffUtilCallback) {

    private val delegates: List<RecyclerAdapterDelegate> = delegatesList.toList()

    // "Infinite" recycler.
    override fun getItemCount(): Int = if (dataProvider.isInitialized()) Int.MAX_VALUE else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder {
        return delegates.getOrNull(viewType)?.onCreateViewHolder(parent) ?: run {
            BaseRecyclerAdapter.createDefaultItem(parent)
        }
    }

    override fun onBindViewHolder(
        holder: BaseRecyclerViewHolder,
        position: Int,
    ) = holder.bind(dataProvider.getItem(position.toShift()), emptyList())

    override fun onBindViewHolder(
        holder: BaseRecyclerViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) = holder.bind(dataProvider.getItem(position.toShift()), payloads)

    override fun getItemViewType(position: Int): Int =
        delegates.indexOfFirst { it.isForValidType(dataProvider.getCurrentItem()) }

    interface DataProvider {
        fun isInitialized(): Boolean
        fun getItem(position: Int): Data
        fun getCurrentItem(): Data
    }

    interface Data : ViewHolderType {
        val position: Int

        override fun getUniqueId(): Long = position.toLong()

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