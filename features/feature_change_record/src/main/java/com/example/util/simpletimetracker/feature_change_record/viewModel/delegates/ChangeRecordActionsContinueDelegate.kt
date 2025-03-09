package com.example.util.simpletimetracker.feature_change_record.viewModel.delegates

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordAction.interactor.RecordActionContinueMediator
import com.example.util.simpletimetracker.domain.recordAction.model.RecordQuickAction
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordDelegateBridge
import com.example.util.simpletimetracker.feature_change_record.viewModel.base.ChangeRecordActionsSubDelegate
import com.example.util.simpletimetracker.navigation.Router
import javax.inject.Inject

class ChangeRecordActionsContinueDelegate @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordActionContinueMediator: RecordActionContinueMediator,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
) : ChangeRecordActionsSubDelegate {

    private var bridge: ChangeRecordDelegateBridge? = null
    private var viewData: List<ViewHolderType> = emptyList()

    override fun attach(bridge: ChangeRecordDelegateBridge) {
        this.bridge = bridge
    }

    override fun getViewData(): List<ViewHolderType> {
        return viewData
    }

    override suspend fun updateViewData() {
        viewData = loadContinueViewData()
        bridge?.send(ChangeRecordDelegateBridge.Action.UpdateViewData)
    }

    suspend fun onContinueClickDelegate() {
        val params = bridge?.getParams() ?: return
        recordActionContinueMediator.execute(
            recordId = params.continueParams.originalRecordId,
            typeId = params.baseParams.newTypeId,
            timeStarted = params.baseParams.newTimeStarted,
            comment = params.baseParams.newComment,
            tagIds = params.baseParams.newCategoryIds,
        )
        // Exit.
        router.back()
    }

    suspend fun canContinue(): Boolean {
        val params = bridge?.getParams() ?: return false

        // Can't continue future record
        return if (params.baseParams.newTimeStarted > System.currentTimeMillis()) {
            bridge?.send(ChangeRecordDelegateBridge.Action.ShowMessage(R.string.cannot_be_in_the_future))
            false
        } else {
            true
        }
    }

    private suspend fun loadContinueViewData(): List<ViewHolderType> {
        val params = bridge?.getParams() ?: return emptyList()
        if (!params.continueParams.isAvailable) return emptyList()
        if (prefsInteractor.getRetroactiveTrackingMode()) return emptyList()
        val isDarkTheme = prefsInteractor.getDarkMode()

        val result = mutableListOf<ViewHolderType>()
        result += HintViewData(
            text = resourceRepo.getString(R.string.change_record_continue_hint),
        )
        result += changeRecordViewDataMapper.mapRecordActionButton(
            action = RecordQuickAction.CONTINUE,
            isEnabled = params.baseParams.isButtonEnabled,
            isDarkTheme = isDarkTheme,
        )
        return result
    }
}