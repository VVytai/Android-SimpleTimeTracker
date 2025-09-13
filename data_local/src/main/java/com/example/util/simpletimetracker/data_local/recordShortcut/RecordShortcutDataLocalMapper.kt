package com.example.util.simpletimetracker.data_local.recordShortcut

import com.example.util.simpletimetracker.data_local.recordTag.RecordShortcutToRecordTagDBO
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import javax.inject.Inject

class RecordShortcutDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordShortcutWithRecordTagsDBO): RecordShortcut {
        return RecordShortcut(
            id = dbo.shortcut.id,
            typeId = dbo.shortcut.typeId,
            comment = dbo.shortcut.comment,
            tags = dbo.recordTags.map(::map),
        )
    }

    fun map(domain: RecordShortcut): RecordShortcutDBO {
        return RecordShortcutDBO(
            id = domain.id,
            typeId = domain.typeId,
            comment = domain.comment,
        )
    }

    fun map(dbo: RecordShortcutToRecordTagDBO): RecordBase.Tag {
        return RecordBase.Tag(
            tagId = dbo.recordTagId,
            numericValue = dbo.recordTagNumericValue,
        )
    }
}