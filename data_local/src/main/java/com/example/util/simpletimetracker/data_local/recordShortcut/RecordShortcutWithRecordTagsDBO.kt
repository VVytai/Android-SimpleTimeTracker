package com.example.util.simpletimetracker.data_local.recordShortcut

import androidx.room.Embedded
import androidx.room.Relation
import com.example.util.simpletimetracker.data_local.recordTag.RecordShortcutToRecordTagDBO
import com.example.util.simpletimetracker.data_local.recordTag.RecordToRecordTagDBO

data class RecordShortcutWithRecordTagsDBO(
    @Embedded
    val shortcut: RecordShortcutDBO,
    @Relation(
        parentColumn = "id",
        entityColumn = "shortcut_id",
        entity = RecordShortcutToRecordTagDBO::class,
    )
    val recordTags: List<RecordShortcutToRecordTagDBO>,
)