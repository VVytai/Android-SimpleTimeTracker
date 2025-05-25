package com.example.util.simpletimetracker.domain.record.interactor

import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import javax.inject.Inject

class RemoveRecordMediator @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun remove(recordId: Long, typeId: Long) {
        remove(
            recordIds = listOf(recordId),
            typeIds = listOf(typeId),
        )
    }

    suspend fun remove(recordIds: List<Long>, typeIds: List<Long>) {
        recordIds.forEach { recordInteractor.remove(it) }
        doAfterRemove(typeIds)
    }

    suspend fun doAfterRemove(typeIds: List<Long>) {
        externalViewsInteractor.onRecordRemove(typeIds)
    }
}