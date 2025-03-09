package com.example.util.simpletimetracker.feature_change_record.viewModel

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordAction.model.RecordQuickAction
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import javax.inject.Inject

class ChangeRecordActionsMoveDelegate @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
) : ChangeRecordActionsSubDelegate<ChangeRecordActionsMoveDelegate.Parent> {

    private var parent: Parent? = null
    private var viewData: List<ViewHolderType> = emptyList()

    override fun attach(parent: Parent) {
        this.parent = parent
    }

    override fun getViewData(): List<ViewHolderType> {
        return viewData
    }

    override suspend fun updateViewData() {
        viewData = loadViewData()
        parent?.update()
    }

    suspend fun onMoveClickDelegate() {
        val params = parent?.getViewDataParams() ?: return
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val showSeconds = prefsInteractor.getShowSeconds()

        router.navigate(
            DateTimeDialogParams(
                tag = MOVE_TIME_STARTED_TAG,
                timestamp = params.newTimeStarted,
                type = DateTimeDialogType.DATETIME(),
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek,
                showSeconds = showSeconds,
            ),
        )
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        val params = parent?.getViewDataParams()
            ?: return emptyList()
        if (!params.isAvailable) return emptyList()
        val isDarkTheme = prefsInteractor.getDarkMode()

        val result = mutableListOf<ViewHolderType>()
        result += HintViewData(
            text = resourceRepo.getString(R.string.change_record_move_hint),
        )
        result += changeRecordViewDataMapper.mapRecordActionButton(
            action = RecordQuickAction.MOVE,
            isEnabled = params.isButtonEnabled,
            isDarkTheme = isDarkTheme,
        )
        return result
    }

    interface Parent {

        fun getViewDataParams(): ViewDataParams?
        fun update()

        data class ViewDataParams(
            val newTimeStarted: Long,
            val isAvailable: Boolean,
            val isButtonEnabled: Boolean,
        )
    }

    companion object {
        const val MOVE_TIME_STARTED_TAG = "move_time_started_tag"
    }
}