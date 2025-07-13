package com.example.util.simpletimetracker.feature_notification.recordType.interactor

import com.example.util.simpletimetracker.core.interactor.CompleteTypesStateInteractor
import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.domain.base.REPEAT_BUTTON_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivitySwitchInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.interactor.NeedTagValueSelectionInteractor
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager
import com.example.util.simpletimetracker.feature_notification.core.TAG_VALUE_DECIMAL_DELIMITER
import kotlinx.coroutines.delay
import javax.inject.Inject

class ActivityStartStopFromBroadcastInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationActivitySwitchInteractor: NotificationActivitySwitchInteractor,
    private val recordRepeatInteractor: RecordRepeatInteractor,
    private val completeTypesStateInteractor: CompleteTypesStateInteractor,
    private val needTagValueSelectionInteractor: NeedTagValueSelectionInteractor,
) {

    suspend fun onActionActivityStop(
        typeId: Long,
    ) {
        val runningRecord = runningRecordInteractor.get(typeId) ?: run {
            notificationTypeInteractor.checkAndHide(typeId)
            return // Not running.
        }

        removeRunningRecordMediator.removeWithRecordAdd(
            runningRecord = runningRecord,
        )
    }

    suspend fun onActionTypeClick(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        typesShift: Int,
    ) {
        if (selectedTypeId == REPEAT_BUTTON_ITEM_ID) {
            recordRepeatInteractor.repeat()
            return
        }

        val started = addRunningRecordMediator.tryStartTimer(
            typeId = selectedTypeId,
            // Switch controls are updated separately right from here,
            // so no need to update after record change.
            updateNotificationSwitch = false,
            commentInputAvailable = false, // TODO open activity? Or RemoteInput?
        ) {
            // Update to show tag selection.
            update(
                from = from,
                typesShift = typesShift,
                tagsShift = 0,
                selectedTypeId = selectedTypeId,
            )
        }
        if (started) {
            val type = recordTypeInteractor.get(selectedTypeId)
            if (type?.defaultDuration.orZero() > 0) {
                completeTypesStateInteractor.notificationTypeIds += selectedTypeId
                update(from, typesShift)
                delay(500)
                completeTypesStateInteractor.notificationTypeIds -= selectedTypeId
                update(from, typesShift)
            } else {
                update(from, typesShift)
            }
        }
    }

    suspend fun onActionTagClick(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        tagId: Long,
        typesShift: Int,
    ) {
        // id = 0 is untagged.
        val needValueSelection = tagId != 0L && needTagValueSelectionInteractor.execute(
            selectedTagIds = emptyList(),
            clickedTagId = tagId,
        )
        if (needValueSelection) {
            update(
                from = from,
                typesShift = typesShift,
                selectedTypeId = selectedTypeId,
                selectedTagId = tagId,
            )
        } else {
            startFromTagSelection(
                from = from,
                selectedTypeId = selectedTypeId,
                tagId = tagId,
                tagValue = null,
                typesShift = typesShift,
            )
        }
    }

    suspend fun onActionTagValueSave(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        tagId: Long,
        tagValue: String?,
        typesShift: Int,
    ) {
        // toDoubleOrNull need a dot as a separator.
        val actualTagValue = tagValue
            ?.replace(TAG_VALUE_DECIMAL_DELIMITER, '.')
            ?.toDoubleOrNull()
        startFromTagSelection(
            from = from,
            selectedTypeId = selectedTypeId,
            tagId = tagId,
            tagValue = actualTagValue,
            typesShift = typesShift,
        )
    }

    suspend fun onRequestUpdate(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        selectedTagId: Long,
        selectedTagValue: String?,
        typesShift: Int,
        tagsShift: Int,
    ) {
        update(
            from = from,
            typesShift = typesShift,
            tagsShift = tagsShift,
            selectedTypeId = selectedTypeId,
            selectedTagId = selectedTagId,
            selectedTagValue = selectedTagValue,
        )
    }

    private suspend fun startFromTagSelection(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        tagId: Long,
        tagValue: Double?,
        typesShift: Int,
    ) {
        if (from !is NotificationControlsManager.From.ActivitySwitch) {
            // Hide tag selection on current notification.
            // Switch would be hidden on start timer.
            update(from, typesShift)
        }
        addRunningRecordMediator.startTimer(
            typeId = selectedTypeId,
            comment = "",
            tags = listOfNotNull(tagId.takeUnless { it == 0L }).map {
                RecordBase.Tag(
                    tagId = it,
                    numericValue = tagValue,
                )
            },
        )
    }

    private suspend fun update(
        from: NotificationControlsManager.From,
        typesShift: Int,
        tagsShift: Int = 0,
        selectedTypeId: Long = 0,
        selectedTagId: Long = 0,
        selectedTagValue: String? = null,
    ) {
        when (from) {
            is NotificationControlsManager.From.ActivityNotification -> {
                val typeId = from.recordTypeId
                if (typeId == 0L) return
                notificationTypeInteractor.checkAndShow(
                    typeId = from.recordTypeId,
                    typesShift = typesShift,
                    tagsShift = tagsShift,
                    selectedTypeId = selectedTypeId,
                    selectedTagId = selectedTagId,
                    selectedTagValue = selectedTagValue,
                )
            }
            is NotificationControlsManager.From.ActivitySwitch -> {
                notificationActivitySwitchInteractor.updateNotification(
                    typesShift = typesShift,
                    tagsShift = tagsShift,
                    selectedTypeId = selectedTypeId,
                    selectedTagId = selectedTagId,
                    selectedTagValue = selectedTagValue,
                )
            }
        }
    }
}