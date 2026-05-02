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
import com.example.util.simpletimetracker.domain.favourite.interactor.RecordTypeToFavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.favourite.model.FavouriteComment
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.recordComment.RecordCommentViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.get

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
            favouriteCommentInteractor.get(newComment)?.id
                ?.let { favouriteCommentInteractor.remove(it) }
                ?: run {
                    val new = FavouriteComment(comment = newComment)
                    favouriteCommentInteractor.add(new)
                }
            updateCommentsViewData()
        }
    }

    override fun onFavouriteCommentLongClick() {
        if (newComment.isEmpty()) return

        delegateScope.launch {
            val favouriteCommentId = favouriteCommentInteractor.get(newComment)?.id
                ?.also { favouriteCommentInteractor.remove(it) }
                ?: run {
                    val new = FavouriteComment(comment = newComment)
                    favouriteCommentInteractor.add(new)
                }
            val recordTypes = recordTypeToFavouriteCommentInteractor.getTypes(favouriteCommentId)

            val newTypeId = parent?.getParams()?.recordTypeId
            if (newTypeId != null && newTypeId != 0L) {
                if (newTypeId in recordTypes) {
                    recordTypeToFavouriteCommentInteractor.removeTypes(favouriteCommentId, listOf(newTypeId))
                } else {
                    recordTypeToFavouriteCommentInteractor.addTypes(favouriteCommentId, listOf(newTypeId))
                }
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