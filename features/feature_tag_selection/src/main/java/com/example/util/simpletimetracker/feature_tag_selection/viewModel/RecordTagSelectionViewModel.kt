package com.example.util.simpletimetracker.feature_tag_selection.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.AddTagToTypeIfNotExistMediator
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordComment.RecordCommentViewData
import com.example.util.simpletimetracker.feature_tag_selection.interactor.RecordTagSelectionViewDataInteractor
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordTagSelectionViewModel @Inject constructor(
    private val viewDataInteractor: RecordTagSelectionViewDataInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val prefsInteractor: PrefsInteractor,
    private val addTagToTypeIfNotExistMediator: AddTagToTypeIfNotExistMediator,
) : BaseViewModel() {

    lateinit var extra: RecordTagSelectionParams

    val viewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadViewData(fromCommentChange = false)
            }
            initial
        }
    }
    val saveButtonVisibility: LiveData<Boolean> by lazySuspend { loadButtonVisibility() }
    val saveClicked: LiveData<Unit> = MutableLiveData()

    private var newComment: String = ""
    private var newCategoryIds: MutableList<Long> = mutableListOf()
    private var searchLoadJob: Job? = null

    // Keep in mind that tags would be added to new types only if show all was selected before,
    // for optimisation reasons, to not call on every save.
    private var showAllTags: Boolean = false

    fun onCategoryClick(item: CategoryViewData) {
        viewModelScope.launch {
            when (item) {
                is CategoryViewData.Record.Tagged -> {
                    newCategoryIds.addOrRemove(item.id)
                }
                is CategoryViewData.Record.Untagged -> {
                    newCategoryIds.clear()
                }
                else -> return@launch
            }
            if (prefsInteractor.getRecordTagSelectionCloseAfterOne()) {
                saveClicked()
            } else {
                updateViewData()
            }
        }
    }

    fun onShowAllTagsClick() = viewModelScope.launch {
        showAllTags = true
        updateViewData()
    }

    fun onShowSuggestionsClick() = viewModelScope.launch {
        val newValue = !prefsInteractor.getIsCommentSelectionSuggestionsEnabled()
        prefsInteractor.setIsCommentSelectionSuggestionsEnabled(newValue)
        updateViewData()
    }

    fun onSaveClick() {
        viewModelScope.launch {
            saveClicked()
        }
    }

    fun onCommentClick(item: RecordCommentViewData) {
        viewModelScope.launch {
            if (item.text != newComment) {
                newComment = item.text
                updateViewData()
            }
        }
    }

    fun onCommentChange(comment: String) {
        viewModelScope.launch {
            if (comment != newComment) {
                newComment = comment
                updateViewData(fromCommentChange = true)
            }
        }
    }

    private suspend fun saveClicked() {
        addRunningRecordMediator.startTimer(
            typeId = extra.typeId,
            tagIds = newCategoryIds,
            comment = newComment,
        )
        if (showAllTags) {
            addTagToTypeIfNotExistMediator.execute(
                typeId = extra.typeId,
                tagIds = newCategoryIds,
            )
        }
        saveClicked.set(Unit)
    }

    private suspend fun loadButtonVisibility(): Boolean {
        val closeAfterOneTag = prefsInteractor.getRecordTagSelectionCloseAfterOne()
        val showTags = RecordTagSelectionParams.Field.Tags in extra.fields
        val showCommentInput = RecordTagSelectionParams.Field.Comment in extra.fields

        return when {
            showTags -> !closeAfterOneTag
            showCommentInput -> true
            else -> false
        }
    }

    private fun updateViewData(
        fromCommentChange: Boolean = false,
    ) {
        searchLoadJob?.cancel()
        searchLoadJob = viewModelScope.launch {
            val data = loadViewData(fromCommentChange)
            viewData.set(data)
        }
    }

    private suspend fun loadViewData(
        fromCommentChange: Boolean,
    ): List<ViewHolderType> {
        return viewDataInteractor.getViewData(
            extra = extra,
            selectedTags = newCategoryIds,
            showAllTags = showAllTags,
            comment = newComment,
            fromCommentChange = fromCommentChange,
        )
    }
}
