package com.example.util.simpletimetracker.feature_base_adapter.commentField

import android.text.TextWatcher
import androidx.core.widget.doAfterTextChanged
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setMargins
import com.example.util.simpletimetracker.feature_base_adapter.commentField.CommentFieldViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemCommentFieldBinding as Binding

fun createCommentFieldAdapterDelegate(
    afterTextChange: (String) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        if (item.text != null &&
            item.text != etCommentItemField.text.toString()
        ) {
            etCommentItemField.setText(item.text)
            etCommentItemField.setSelection(item.text.length)
        }
        inputCommentField.setMargins(
            top = item.marginTopDp,
            start = item.marginHorizontal,
            end = item.marginHorizontal,
        )
        etCommentItemField.removeTextChangedListener(textWatcher)
        textWatcher = etCommentItemField.doAfterTextChanged { afterTextChange(it.toString()) }
    }
}

data class CommentFieldViewData(
    val id: Long,
    val text: String?,
    val marginTopDp: Int,
    val marginHorizontal: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ViewData
}

private var textWatcher: TextWatcher? = null