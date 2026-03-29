package com.example.util.simpletimetracker.feature_change_shortcut.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.RecordCommentSearchViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.RecordTagViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.RecordTypesViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate
import com.example.util.simpletimetracker.core.viewData.CommentFilterTypeViewData
import com.example.util.simpletimetracker.domain.base.suspendLazy
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.favourite.interactor.FavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.favourite.model.FavouriteComment
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordAction.interactor.RecordActionShortcutMediator
import com.example.util.simpletimetracker.domain.recordShortcut.interactor.RecordShortcutInteractor
import com.example.util.simpletimetracker.domain.recordShortcut.interactor.ShortcutsDataUpdateInteractor
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.domain.recordTag.interactor.NeedTagValueSelectionInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordComment.RecordCommentViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_shortcut.R
import com.example.util.simpletimetracker.feature_change_shortcut.adapter.ChangeShortcutSettingActionViewData
import com.example.util.simpletimetracker.feature_change_shortcut.interactor.ChangeShortcutViewDataInteractor
import com.example.util.simpletimetracker.feature_change_shortcut.mapper.ChangeShortcutViewDataMapper
import com.example.util.simpletimetracker.feature_change_shortcut.viewData.ChangeShortcutChooserState
import com.example.util.simpletimetracker.feature_change_shortcut.viewData.ChangeShortcutViewData
import com.example.util.simpletimetracker.feature_change_shortcut.viewData.ChangeShortcutViewData.TargetModeButtonViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeShortcutParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagValueSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeShortcutViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val recordShortcutInteractor: RecordShortcutInteractor,
    private val recordActionShortcutMediator: RecordActionShortcutMediator,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypesViewDataInteractor: RecordTypesViewDataInteractor,
    private val recordTagViewDataInteractor: RecordTagViewDataInteractor,
    private val recordCommentSearchViewDataInteractor: RecordCommentSearchViewDataInteractor,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val favouriteCommentInteractor: FavouriteCommentInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeToTagInteractor: RecordTypeToTagInteractor,
    private val needTagValueSelectionInteractor: NeedTagValueSelectionInteractor,
    private val shortcutsDataUpdateInteractor: ShortcutsDataUpdateInteractor,
    private val viewDataInteractor: ChangeShortcutViewDataInteractor,
    private val viewDataMapper: ChangeShortcutViewDataMapper,
    private val snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
) : BaseViewModel() {

    lateinit var extra: ChangeShortcutParams

    val viewData: LiveData<ChangeShortcutViewData> = MutableLiveData()
    val chooserState: LiveData<ViewChooserStateDelegate.States> = MutableLiveData(
        ViewChooserStateDelegate.States(
            current = ChangeShortcutChooserState.Closed,
            previous = ChangeShortcutChooserState.Closed,
        ),
    )
    val types: LiveData<List<ViewHolderType>> = MutableLiveData(emptyList())
    val tags: LiveData<List<ViewHolderType>> = MutableLiveData(emptyList())
    val comments: LiveData<List<ViewHolderType>> = MutableLiveData(emptyList())
    val settingActions: LiveData<List<ViewHolderType>> = MutableLiveData(emptyList())
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(shortcutId != 0L) }
    val keyboardVisibility: LiveData<Boolean> = MutableLiveData(false)

    private val typesCache = suspendLazy { recordTypeInteractor.getAll() }
    private val tagCache = suspendLazy { recordTagInteractor.getAll() }

    private var initialized: Boolean = false
    private var updateJob: Job? = null
    private var commentLoadJob: Job? = null
    private val shortcutId: Long get() = (extra as? ChangeShortcutParams.Change)?.id.orZero()
    private var targetMode: RecordShortcut.TargetMode = RecordShortcut.TargetMode.Record
    private var recordTypeId: Long? = null
    private var recordTags: List<RecordBase.Tag> = emptyList()
    private var recordComment: String = ""
    private var settingAction: RecordShortcut.SettingAction? = null

    fun initialize() {
        if (initialized) return
        viewModelScope.launch {
            initializeData()
            updateViewData()
            updateChoosersViewData()
            initialized = true
        }
    }

    fun onButtonsRowClick(data: ButtonsRowViewData) {
        val mode = (data as? TargetModeButtonViewData)?.mode ?: return
        if (targetMode == mode) return
        targetMode = mode
        closeChoosers()
        updateViewData()
    }

    fun onTypeChooserClick() {
        onNewChooserState(ChangeShortcutChooserState.Activity)
    }

    fun onTagChooserClick() {
        onNewChooserState(ChangeShortcutChooserState.Tag)
    }

    fun onCommentChooserClick() {
        onNewChooserState(ChangeShortcutChooserState.Comment)
    }

    fun onSettingActionChooserClick() {
        onNewChooserState(ChangeShortcutChooserState.SettingAction)
    }

    fun onTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            if (item.id != recordTypeId) {
                recordTypeId = item.id
                recordTags = emptyList()
                updateViewData()
                updateTagsViewData()
                updateCommentsViewData()
            }

            onTypeChooserClick()
            openTagSelectionIfNeeded()
        }
    }

    fun onTagClick(item: CategoryViewData) {
        viewModelScope.launch {
            when (item) {
                is CategoryViewData.Record.Tagged -> {
                    val needValueSelection = needTagValueSelectionInteractor.execute(
                        selectedTagIds = recordTags.map(RecordBase.Tag::tagId),
                        clickedTagId = item.id,
                    )
                    if (needValueSelection) {
                        RecordTagValueSelectionParams(
                            tag = CHANGE_SHORTCUT_TAG_VALUE_SELECTION,
                            tagId = item.id,
                        ).let(router::navigate)
                        return@launch
                    }

                    recordTags = recordTags.addOrRemove(item.id)
                }
                is CategoryViewData.Record.Untagged -> {
                    recordTags = emptyList()
                }
                else -> return@launch
            }

            updateViewData()
            updateTagsViewData()
        }
    }

    fun onTagValueSelected(params: RecordTagValueSelectionParams, value: Double) {
        if (params.tag != CHANGE_SHORTCUT_TAG_VALUE_SELECTION) return
        viewModelScope.launch {
            recordTags = recordTags
                .filterNot { it.tagId == params.tagId } +
                RecordBase.Tag(
                    tagId = params.tagId,
                    numericValue = value,
                )
            updateViewData()
            updateTagsViewData()
        }
    }

    fun onCommentClick(item: RecordCommentViewData) {
        viewModelScope.launch {
            if (item.text == recordComment) return@launch
            recordComment = item.text
            updateViewData()
            updateCommentsViewData()
        }
    }

    fun onCommentFilterClick(item: FilterViewData) = viewModelScope.launch {
        val data = item.type as? CommentFilterTypeViewData ?: return@launch
        val type = recordCommentSearchViewDataInteractor.map(data)
        val newFilters = prefsInteractor.getHiddenCommentFilters().toMutableSet()
        newFilters.addOrRemove(type)
        prefsInteractor.setHiddenCommentFilters(newFilters)
        updateCommentsViewData()
    }

    fun onFavouriteCommentClick() {
        if (recordComment.isEmpty()) return
        viewModelScope.launch {
            favouriteCommentInteractor.get(recordComment)
                ?.let { favouriteCommentInteractor.remove(it.id) }
                ?: run {
                    val favourite = FavouriteComment(comment = recordComment)
                    favouriteCommentInteractor.add(favourite)
                }
            updateCommentsViewData()
        }
    }

    fun onCommentChange(comment: String) {
        if (recordComment == comment) return
        recordComment = comment
        updateViewData()
        updateCommentsViewData(fromCommentChange = true)
    }

    fun onSettingActionClick(data: ChangeShortcutSettingActionViewData) {
        val action = data.action
        if (settingAction == action) return
        settingAction = action
        updateViewData()
        closeChoosers()
    }

    // TODO move tag and comment to delegates
    // TODO remove change_record_feature from gradle deps
    // TODO add ShortcutView and replace preview with it, so hint would be visible
    // TODO add more settings
    // TODO add tests
    fun onSaveClick() {
        val target = buildTarget() ?: return
        saveButtonEnabled.set(false)
        viewModelScope.launch {
            if (shortcutId != 0L) {
                recordShortcutInteractor.update(
                    RecordShortcut(
                        id = shortcutId,
                        target = target,
                    ),
                )
            } else {
                recordActionShortcutMediator.execute(target)
            }
            shortcutsDataUpdateInteractor.send()
            router.back()
        }
    }

    fun onDeleteClick() {
        router.navigate(
            StandardDialogParams(
                tag = DELETE_SHORTCUT_ALERT_DIALOG_TAG,
                title = resourceRepo.getString(R.string.change_record_type_delete_alert),
                message = resourceRepo.getString(R.string.archive_deletion_alert),
                btnPositive = resourceRepo.getString(R.string.ok),
                btnNegative = resourceRepo.getString(R.string.cancel),
            ),
        )
    }

    fun onBack() {
        if (chooserState.value?.current !is ChangeShortcutChooserState.Closed) {
            closeChoosers()
        } else {
            router.back()
        }
    }

    fun onPositiveClick(tag: String?) {
        if (tag != DELETE_SHORTCUT_ALERT_DIALOG_TAG) return
        router.back() // Close dialog.
        if (shortcutId == 0L) return
        deleteButtonEnabled.set(false)
        viewModelScope.launch {
            recordShortcutInteractor.remove(shortcutId)
            shortcutsDataUpdateInteractor.send()
            keyboardVisibility.set(false)
            router.back()
        }
    }

    private fun buildTarget(): RecordShortcut.Target? {
        val params = ChangeShortcutViewDataInteractor.Params(
            targetMode = targetMode,
            recordTypeId = recordTypeId,
            recordTags = recordTags,
            comment = recordComment,
            settingAction = settingAction,
        )
        val target = viewDataInteractor.buildTarget(params)
        return when {
            target.noTypeId -> {
                showMessage(R.string.change_record_message_choose_type)
                null
            }
            target.noSettingAction -> {
                showMessage(R.string.change_complex_rule_choose_action)
                null
            }
            else -> target.data
        }
    }

    private fun showMessage(stringResId: Int) {
        snackBarMessageNavigationInteractor.showMessage(stringResId)
    }

    private suspend fun openTagSelectionIfNeeded() {
        if (targetMode != RecordShortcut.TargetMode.Record) return
        val typeId = recordTypeId ?: return
        val tagsById = recordTagInteractor.getAll().associateBy { it.id }
        val tagsForType = recordTypeToTagInteractor.getTags(typeId)
            .mapNotNull(tagsById::get)
            .filterNot { it.archived }

        if (tagsForType.isNotEmpty()) {
            delay(300)
            onTagChooserClick()
        }
    }

    private fun onNewChooserState(state: ChangeShortcutChooserState) {
        val current = chooserState.value?.current ?: ChangeShortcutChooserState.Closed
        val newState = if (current == state) {
            ChangeShortcutChooserState.Closed
        } else {
            state
        }

        // Show keyboard on comment chooser opened, hide otherwise.
        keyboardVisibility.set(newState is ChangeShortcutChooserState.Comment)

        chooserState.set(
            ViewChooserStateDelegate.States(
                current = newState,
                previous = current,
            ),
        )
    }

    private fun closeChoosers() {
        val current = chooserState.value?.current ?: ChangeShortcutChooserState.Closed
        keyboardVisibility.set(false)
        chooserState.set(
            ViewChooserStateDelegate.States(
                current = ChangeShortcutChooserState.Closed,
                previous = current,
            ),
        )
    }

    private suspend fun initializeData() {
        recordShortcutInteractor.get(shortcutId)?.let { shortcut ->
            val target = shortcut.target
            targetMode = target.mode
            when (target) {
                is RecordShortcut.Target.Record -> {
                    recordTypeId = target.typeId
                    recordTags = target.tags
                    recordComment = target.comment
                }
                is RecordShortcut.Target.Setting -> {
                    settingAction = target.action
                }
            }
        }
    }

    private suspend fun updateChoosersViewData() {
        updateTypesViewData()
        updateTagsViewData()
        updateActionsViewData()
        updateCommentsViewData()
    }

    private fun updateViewData() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            val data = viewDataInteractor.getViewData(
                params = ChangeShortcutViewDataInteractor.Params(
                    targetMode = targetMode,
                    recordTypeId = recordTypeId,
                    recordTags = recordTags,
                    comment = recordComment,
                    settingAction = settingAction,
                ),
                typesMap = typesCache().associateBy(RecordType::id),
                tags = tagCache(),
            )
            viewData.set(data)
        }
    }

    private suspend fun updateTypesViewData() {
        types.set(recordTypesViewDataInteractor.getTypesViewData())
    }

    private suspend fun updateTagsViewData() {
        val data = recordTagViewDataInteractor.getViewData(
            selectedTags = recordTags,
            typeIds = listOf(recordTypeId.orZero()),
            showAllTags = false,
            multipleChoiceAvailable = true,
            showAddButton = false,
            showHint = true,
            showArchived = false,
            showUntaggedButton = true,
            showAllTagsButton = false,
        )
        tags.set(data.data)
    }

    private fun updateCommentsViewData(
        fromCommentChange: Boolean = false,
    ) {
        commentLoadJob?.cancel()
        commentLoadJob = viewModelScope.launch {
            val data = changeRecordViewDataInteractor.getCommentsViewData(
                comment = recordComment,
                typeId = recordTypeId.orZero(),
                fromCommentChange = fromCommentChange,
            )
            comments.set(data)
        }
    }

    private fun updateActionsViewData() {
        val settingsOrder = recordShortcutInteractor.getSettingsOrder()
        val data = viewDataMapper.mapSettingActions(actionsOrder = settingsOrder)
        settingActions.set(data)
    }

    companion object {
        private const val CHANGE_SHORTCUT_TAG_VALUE_SELECTION = "CHANGE_SHORTCUT_TAG_VALUE_SELECTION"
        private const val DELETE_SHORTCUT_ALERT_DIALOG_TAG = "DELETE_SHORTCUT_ALERT_DIALOG_TAG"
    }
}
