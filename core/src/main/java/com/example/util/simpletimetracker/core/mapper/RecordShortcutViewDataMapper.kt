package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordShortcut.RecordShortcutViewData
import javax.inject.Inject

class RecordShortcutViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val recordTagFullNameMapper: RecordTagFullNameMapper,
    private val recordViewDataMapper: RecordViewDataMapper,
) {

    fun map(
        shortcut: RecordShortcut,
        recordType: RecordType,
        recordTags: List<RecordTag>,
        isDarkTheme: Boolean,
    ): RecordShortcutViewData {
        val tagIds = shortcut.tags.map(RecordBase.Tag::tagId)

        // Shortcut view is simplified record view.
        // So use record data without some data.
        val data = RecordViewData.Tracked(
            id = 0,
            timeStartedTimestamp = 0,
            timeEndedTimestamp = 0,
            name = recordType.name,
            tagName = recordTagFullNameMapper.getFullName(
                tags = recordTags.filter { it.id in tagIds },
                tagData = shortcut.tags,
            ),
            timeStarted = "",
            timeFinished = "",
            duration = "",
            iconId = iconMapper.mapIcon(recordType.icon),
            color = colorMapper.mapToColorInt(
                color = recordType.color,
                isDarkTheme = isDarkTheme,
            ),
            comment = shortcut.comment,
            // TODO SHORT move to one line after "name - tag"?
            // TODO SHORT remove "Shortcut" label and add hint?
            // TODO SHORT width wrapContent?
            // TODO SHORT show filtered if already started?
            // TODO SHORT translate strings
            // TODO SHORT add message after adding
        )

        return RecordShortcutViewData(
            id = shortcut.id,
            record = data,
        )
    }

    fun mapFiltered(
        viewData: RecordShortcutViewData,
        isDarkTheme: Boolean,
        isFiltered: Boolean,
    ): RecordShortcutViewData {
        return if (isFiltered) {
            val newRecordData = recordViewDataMapper.mapFiltered(
                viewData = viewData.record,
                isDarkTheme = isDarkTheme,
                isFiltered = true
            )
            viewData.copy(record = newRecordData)
        } else {
            viewData
        }
    }
}