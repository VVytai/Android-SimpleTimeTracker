package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordShortcut.RecordShortcutViewData
import javax.inject.Inject

class RecordShortcutViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val recordTagFullNameMapper: RecordTagFullNameMapper,
) {

    fun map(
        shortcut: RecordShortcut,
        recordType: RecordType,
        recordTags: List<RecordTag>,
        isDarkTheme: Boolean,
        isFiltered: Boolean,
    ): RecordShortcutViewData {
        val tagIds = shortcut.tags.map(RecordBase.Tag::tagId)

        val icon = iconMapper.mapIcon(recordType.icon)

        val name = listOf(
            recordType.name,
            recordTagFullNameMapper.getFullName(
                tags = recordTags.filter { it.id in tagIds },
                tagData = shortcut.tags,
            ),
            shortcut.comment,
        ).mapNotNull {
            it.takeIf { it.isNotEmpty() }
        }.joinToString(separator = " - ")

        // Shortcut view is simplified category view.
        val data = CategoryViewData.Record.Tagged(
            id = 0,
            name = name,
            iconColor = if (isFiltered) {
                colorMapper.toFilteredIconColor(isDarkTheme)
            } else {
                colorMapper.toIconColor(isDarkTheme)
            },
            color = if (isFiltered) {
                colorMapper.toFilteredColor(isDarkTheme)
            } else {
                colorMapper.mapToColorInt(recordType.color, isDarkTheme)
            },
            icon = icon,
            iconAlpha = colorMapper.toIconAlpha(icon, isFiltered),
            // TODO show tag with alpha color like in record
        )

        return RecordShortcutViewData(
            id = shortcut.id,
            data = data,
        )
    }
}