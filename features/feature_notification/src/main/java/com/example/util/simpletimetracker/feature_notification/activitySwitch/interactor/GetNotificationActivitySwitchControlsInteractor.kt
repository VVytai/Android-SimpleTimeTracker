package com.example.util.simpletimetracker.feature_notification.activitySwitch.interactor

import com.example.util.simpletimetracker.core.interactor.CompleteTypesStateInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTagViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.REPEAT_BUTTON_ITEM_ID
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.TAGS_LIST_SIZE
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.TYPES_LIST_SIZE
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsParams
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class GetNotificationActivitySwitchControlsInteractor @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTagViewDataMapper: RecordTagViewDataMapper,
    private val completeTypesStateInteractor: CompleteTypesStateInteractor,
) {

    fun getControls(
        hint: String,
        isDarkTheme: Boolean,
        types: List<RecordType>,
        suggestions: List<RecordType>,
        showRepeatButton: Boolean,
        typesShift: Int = 0,
        tags: List<RecordTag> = emptyList(),
        tagsShift: Int = 0,
        selectedTypeId: Long? = null,
        goals: Map<Long, List<RecordTypeGoal>>,
        allDailyCurrents: Map<Long, GetCurrentRecordsDurationInteractor.Result>,
    ): NotificationControlsParams {
        val typesMap = types.associateBy { it.id }

        val suggestionsViewData = suggestions.map { type ->
            mapType(
                type = type,
                isDarkTheme = isDarkTheme,
                goals = goals,
                allDailyCurrents = allDailyCurrents,
            )
        }

        val repeatButtonViewData = if (showRepeatButton) {
            mapRepeat(isDarkTheme)
        } else {
            emptyList()
        }

        val typesViewData = types
            .filter { !it.hidden }
            .map { type ->
                mapType(
                    type = type,
                    isDarkTheme = isDarkTheme,
                    goals = goals,
                    allDailyCurrents = allDailyCurrents,
                )
            }

        val tagsViewData = tags
            .filter { !it.archived }
            .map { tag ->
                mapTag(
                    tag = tag,
                    typesMap = typesMap,
                    isDarkTheme = isDarkTheme,
                )
            }
            .let {
                if (it.isNotEmpty()) {
                    mapUntagged(isDarkTheme) + it
                } else {
                    it
                }
            }

        // Populate container with empty items to preserve prev next controls position.
        fun <T> populateWithEmpty(
            data: List<T>,
            pageSize: Int,
            emptyValueProducer: () -> T,
        ): List<T> {
            if (data.isEmpty()) return data
            if (data.size % pageSize == 0) return data
            val emptyCount = pageSize - (data.size % pageSize)
            val emptyData = List(emptyCount) { emptyValueProducer() }
            return data + emptyData
        }

        val allTypesViewData = populateWithEmpty(
            data = suggestionsViewData,
            pageSize = TYPES_LIST_SIZE,
            emptyValueProducer = { NotificationControlsParams.Type.Empty },
        ) + repeatButtonViewData +
            typesViewData

        return NotificationControlsParams.Enabled(
            hint = hint,
            types = populateWithEmpty(
                data = allTypesViewData,
                pageSize = TYPES_LIST_SIZE,
                emptyValueProducer = { NotificationControlsParams.Type.Empty },
            ),
            typesShift = typesShift,
            tags = populateWithEmpty(
                data = tagsViewData,
                pageSize = TAGS_LIST_SIZE,
                emptyValueProducer = { NotificationControlsParams.Tag.Empty },
            ),
            tagsShift = tagsShift,
            controlIconPrev = RecordTypeIcon.Image(R.drawable.arrow_left),
            controlIconNext = RecordTypeIcon.Image(R.drawable.arrow_right),
            controlIconColor = colorMapper.toInactiveColor(isDarkTheme),
            filteredTypeColor = colorMapper.toInactiveColor(isDarkTheme),
            selectedTypeId = selectedTypeId,
        )
    }

    private fun mapType(
        type: RecordType,
        isDarkTheme: Boolean,
        goals: Map<Long, List<RecordTypeGoal>>,
        allDailyCurrents: Map<Long, GetCurrentRecordsDurationInteractor.Result>,
    ): NotificationControlsParams.Type.Present {
        return NotificationControlsParams.Type.Present(
            id = type.id,
            icon = type.icon.let(iconMapper::mapIcon),
            color = type.color.let { colorMapper.mapToColorInt(it, isDarkTheme) },
            checkState = recordTypeViewDataMapper.mapGoalCheckmark(
                type = type,
                goals = goals,
                allDailyCurrents = allDailyCurrents,
            ),
            isComplete = type.id in completeTypesStateInteractor.notificationTypeIds,
        )
    }

    private fun mapRepeat(
        isDarkTheme: Boolean,
    ): List<NotificationControlsParams.Type.Present> {
        val viewData = recordTypeViewDataMapper.mapToRepeatItem(
            numberOfCards = 0,
            isDarkTheme = isDarkTheme,
        )
        return NotificationControlsParams.Type.Present(
            id = REPEAT_BUTTON_ITEM_ID,
            icon = viewData.iconId,
            color = viewData.color,
            checkState = GoalCheckmarkView.CheckState.HIDDEN,
            isComplete = false,
        ).let(::listOf)
    }

    private fun mapTag(
        tag: RecordTag,
        typesMap: Map<Long, RecordType>,
        isDarkTheme: Boolean,
    ): NotificationControlsParams.Tag {
        return NotificationControlsParams.Tag.Present(
            id = tag.id,
            text = tag.name,
            color = recordTagViewDataMapper.mapColor(
                tag = tag,
                types = typesMap,
            ).let { colorMapper.mapToColorInt(it, isDarkTheme) },
        )
    }

    private fun mapUntagged(
        isDarkTheme: Boolean,
    ): List<NotificationControlsParams.Tag> {
        return NotificationControlsParams.Tag.Present(
            id = 0L,
            text = R.string.change_record_untagged.let(resourceRepo::getString),
            color = colorMapper.toUntrackedColor(isDarkTheme),
        ).let(::listOf)
    }
}