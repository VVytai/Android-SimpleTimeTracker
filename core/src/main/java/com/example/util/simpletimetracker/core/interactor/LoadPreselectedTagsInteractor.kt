package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.base.suspendLazy
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class LoadPreselectedTagsInteractor @Inject constructor(
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {

    suspend fun execute(typeId: Long): List<RecordBase.Tag> = coroutineScope {
        val ruleTags = addRunningRecordMediator.processRules(
            typeId = typeId,
            timeStarted = currentTimestampProvider.get(),
            prevRecords = suspendLazy { emptyList() },
        ).tags

        addRunningRecordMediator.getAllTags(
            typeId = typeId,
            currentTags = emptyList(),
            tagValuesFromRules = ruleTags,
        )
    }
}