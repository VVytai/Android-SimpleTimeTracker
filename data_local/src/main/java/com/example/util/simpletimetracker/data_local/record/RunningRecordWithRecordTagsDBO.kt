package com.example.util.simpletimetracker.data_local.record

import androidx.room.Embedded
import androidx.room.Relation
import com.example.util.simpletimetracker.data_local.recordTag.RunningRecordToRecordTagDBO

data class RunningRecordWithRecordTagsDBO(
    @Embedded
    val runningRecord: RunningRecordDBO,
    @Relation(
        parentColumn = "id",
        entityColumn = "running_record_id",
        entity = RunningRecordToRecordTagDBO::class,
        projection = ["record_tag_id"],
    )
    val recordTags: List<Long>,
)