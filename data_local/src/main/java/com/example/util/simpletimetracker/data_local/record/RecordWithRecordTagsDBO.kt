package com.example.util.simpletimetracker.data_local.record

import androidx.room.Embedded
import androidx.room.Relation
import com.example.util.simpletimetracker.data_local.recordTag.RecordToRecordTagDBO

data class RecordWithRecordTagsDBO(
    @Embedded
    val record: RecordDBO,
    @Relation(
        parentColumn = "id",
        entityColumn = "record_id",
        entity = RecordToRecordTagDBO::class,
        projection = ["record_tag_id"],
    )
    val recordTags: List<Long>,
)