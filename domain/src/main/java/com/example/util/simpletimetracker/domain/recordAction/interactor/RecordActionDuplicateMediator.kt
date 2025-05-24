package com.example.util.simpletimetracker.domain.recordAction.interactor

import com.example.util.simpletimetracker.domain.record.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.record.model.Record
import javax.inject.Inject

class RecordActionDuplicateMediator @Inject constructor(
    private val addRecordMediator: AddRecordMediator,
) {

    suspend fun execute(records: List<Record>) {
        records.map { record ->
            Record(
                typeId = record.typeId,
                timeStarted = record.timeStarted,
                timeEnded = record.timeEnded,
                comment = record.comment,
                tagIds = record.tagIds,
            )
        }.let {
            addRecordMediator.add(it)
        }
    }

    suspend fun execute(
        typeId: Long,
        timeStarted: Long,
        timeEnded: Long,
        comment: String,
        tagIds: List<Long>,
    ) {
        Record(
            typeId = typeId,
            timeStarted = timeStarted,
            timeEnded = timeEnded,
            comment = comment,
            tagIds = tagIds,
        ).let {
            addRecordMediator.add(it)
        }
    }
}