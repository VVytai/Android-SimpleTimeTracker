package com.example.util.simpletimetracker.core.delegates.commentSelection.viewModelDelegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.commentSelection.interactor.CommentSelectionDelegateViewDataInteractor
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.RecordCommentSearchViewDataInteractor
import com.example.util.simpletimetracker.core.viewData.CommentFilterTypeViewData
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.favourite.interactor.FavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.favourite.interactor.IsCommentFavouriteInteractor
import com.example.util.simpletimetracker.domain.favourite.interactor.RecordTypeToFavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.favourite.model.FavouriteComment
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.recordComment.RecordCommentViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

interface CommentSelectionViewModelDelegate {
    val comments: LiveData<List<ViewHolderType>>

    fun attach(parent: Parent)
    fun onCommentChange(comment: String)
    fun onCommentFilterClick(item: FilterViewData)
    fun onFavouriteCommentClick()
    fun onFavouriteCommentLongClick()
    fun onCommentClick(item: RecordCommentViewData)

    interface Parent {
        fun getParams(): Params
        suspend fun onCommentClick()
        fun onCommentChange()

        data class Params(
            val recordTypeId: Long?,
        )
    }
}

class CommentSelectionViewModelDelegateImpl @Inject constructor(
    private val commentSelectionDelegateViewDataInteractor: CommentSelectionDelegateViewDataInteractor,
    private val recordCommentSearchViewDataInteractor: RecordCommentSearchViewDataInteractor,
    private val favouriteCommentInteractor: FavouriteCommentInteractor,
    private val recordTypeToFavouriteCommentInteractor: RecordTypeToFavouriteCommentInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val isCommentFavouriteInteractor: IsCommentFavouriteInteractor,
) :
    CommentSelectionViewModelDelegate,
    ViewModelDelegate() {

    override val comments: LiveData<List<ViewHolderType>> = MutableLiveData(emptyList())

    var newComment: String = ""

    private var parent: CommentSelectionViewModelDelegate.Parent? = null
    private var commentLoadJob: Job? = null

    override fun attach(parent: CommentSelectionViewModelDelegate.Parent) {
        this.parent = parent
    }

    override fun onCommentClick(item: RecordCommentViewData) {
        delegateScope.launch {
            if (item.text == newComment) return@launch
            newComment = item.text
            parent?.onCommentClick()
            updateCommentsViewData()
        }
    }

    override fun onCommentFilterClick(item: FilterViewData) {
        delegateScope.launch {
            val data = item.type as? CommentFilterTypeViewData ?: return@launch
            val type = recordCommentSearchViewDataInteractor.map(data)
            val newFilters = prefsInteractor.getHiddenCommentFilters().toMutableSet()
            newFilters.addOrRemove(type)
            prefsInteractor.setHiddenCommentFilters(newFilters.toSet())
            updateCommentsViewData()
        }
    }

    override fun onFavouriteCommentClick() {
        if (newComment.isEmpty()) return

        delegateScope.launch {
            val newTypeId = parent?.getParams()?.recordTypeId.orZero()
            val isFavourite = isCommentFavouriteInteractor.execute(newComment, newTypeId)
            if (isFavourite) {
                removeComment(newComment, 0L)
            } else {
                addComment(newComment, 0L)
            }
            updateCommentsViewData()
        }
    }

    override fun onFavouriteCommentLongClick() {
        if (newComment.isEmpty()) return

        delegateScope.launch {
            val newTypeId = parent?.getParams()?.recordTypeId.orZero()
            val isFavourite = isCommentFavouriteInteractor.execute(newComment, newTypeId)
            if (isFavourite) {
                removeComment(newComment, newTypeId)
            } else {
                addComment(newComment, newTypeId)
            }
            updateCommentsViewData()
        }
    }

    override fun onCommentChange(comment: String) {
        if (comment == newComment) return
        newComment = comment
        parent?.onCommentChange()
        updateCommentsViewData(fromCommentChange = true)
    }

    private suspend fun removeComment(comment: String, typeId: Long) {
        val comment = favouriteCommentInteractor.get(comment) ?: return
        if (typeId == 0L) {
            // General comment - remove it.
            favouriteCommentInteractor.remove(comment.id)
            return
        }
        val newTypeId = parent?.getParams()?.recordTypeId.orZero()
        val typesToComments = recordTypeToFavouriteCommentInteractor.getTypes(comment.id)
        val otherRelations = typesToComments.filter { it != newTypeId }
        if (typesToComments.isEmpty() || otherRelations.isEmpty()) {
            // Comment is general - remove it.
            favouriteCommentInteractor.remove(comment.id)
        } else {
            // Comment assigned to other activities - remove only relation to this activity.
            recordTypeToFavouriteCommentInteractor.removeTypes(
                commentId = comment.id,
                typeIds = listOf(newTypeId),
            )
        }
    }

    private suspend fun addComment(comment: String, typeId: Long) {
        val existingComment = favouriteCommentInteractor.get(comment)
        val commentId = if (existingComment == null) {
            // Does not exist - create.
            val new = FavouriteComment(comment = comment)
            favouriteCommentInteractor.add(new)
        } else {
            // Already exist.
            existingComment.id
        }
        if (typeId == 0L) {
            // Make it general.
            recordTypeToFavouriteCommentInteractor.removeAll(commentId)
        } else {
            // Assign only to this activity.
            recordTypeToFavouriteCommentInteractor.addTypes(
                commentId = commentId,
                typeIds = listOf(typeId),
            )
        }
    }

    fun updateCommentsViewData(
        fromCommentChange: Boolean = false,
    ) {
        commentLoadJob?.cancel()
        commentLoadJob = delegateScope.launch {
            comments.set(loadCommentsViewData(fromCommentChange))
        }
    }

    private suspend fun loadCommentsViewData(
        fromCommentChange: Boolean,
    ): List<ViewHolderType> {
        return commentSelectionDelegateViewDataInteractor.getCommentsViewData(
            comment = newComment,
            typeId = parent?.getParams()?.recordTypeId.orZero(),
            fromCommentChange = fromCommentChange,
        )
    }
}