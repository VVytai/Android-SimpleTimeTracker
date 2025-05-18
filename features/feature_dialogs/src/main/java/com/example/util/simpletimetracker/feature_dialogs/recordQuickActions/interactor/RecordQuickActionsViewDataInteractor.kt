package com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.interactor

import android.text.SpannableStringBuilder
import androidx.core.text.bold
import com.example.util.simpletimetracker.core.mapper.RecordQuickActionMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.recordTag.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordAction.model.RecordQuickAction
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.RecordQuickActionsButtonBigViewData
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.RecordQuickActionsButtonViewData
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.RecordQuickActionsWidthHolder
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model.RecordQuickActionsButton
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model.RecordQuickActionsState
import com.example.util.simpletimetracker.feature_views.extension.image
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams.Type
import javax.inject.Inject

class RecordQuickActionsViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordQuickActionMapper: RecordQuickActionMapper,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
) {

    suspend fun getRecord(
        extra: RecordQuickActionsParams,
    ): RecordBase? {
        return when (val params = extra.type) {
            is Type.RecordTracked -> recordInteractor.get(params.id)
            is Type.RecordUntracked -> null
            is Type.RecordRunning -> runningRecordInteractor.get(params.id)
            null -> null
        }
    }

    suspend fun getViewData(
        extra: RecordQuickActionsParams,
    ): RecordQuickActionsState {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val retroactiveTrackingModeEnabled = prefsInteractor.getRetroactiveTrackingMode()
        val canContinue = !retroactiveTrackingModeEnabled
        val typeId = getRecord(extra)?.typeIds?.firstOrNull()
        val hasTags = typeId
            ?.let { getSelectableTagsInteractor.execute(it) }
            .orEmpty().any { !it.archived }
        val allowedButtons = getAllowedButtons(
            extra = extra,
            canContinue = canContinue,
            hasTags = hasTags,
        )
        val buttons = getAllButtons(
            allowedButtons = allowedButtons,
        ).let(::applyWidth)
        val hintData = mapHint(
            allowedButtons = allowedButtons,
            isDarkTheme = isDarkTheme,
        )

        return RecordQuickActionsState(
            buttons = buttons,
            hintData = hintData,
        )
    }

    private fun getAllButtons(
        allowedButtons: List<RecordQuickActionsButton>,
    ): List<ViewHolderType> {
        val allActions = listOf(
            RecordQuickActionsButton.STATISTICS,
            RecordQuickActionsButton.DELETE,
            RecordQuickActionsButton.CONTINUE,
            RecordQuickActionsButton.REPEAT,
            RecordQuickActionsButton.DUPLICATE,
            RecordQuickActionsButton.MOVE,
            RecordQuickActionsButton.MERGE,
            RecordQuickActionsButton.STOP,
            RecordQuickActionsButton.CHANGE_ACTIVITY,
            RecordQuickActionsButton.CHANGE_TAG,
        ).filter {
            it in allowedButtons
        }

        return allActions.mapNotNull {
            when (it) {
                RecordQuickActionsButton.STATISTICS -> {
                    RecordQuickActionsButtonBigViewData(
                        block = it,
                        text = R.string.shortcut_navigation_statistics.let(resourceRepo::getString),
                        icon = R.drawable.statistics,
                    )
                }
                RecordQuickActionsButton.DELETE -> {
                    RecordQuickActionsButtonBigViewData(
                        block = it,
                        text = R.string.archive_dialog_delete.let(resourceRepo::getString),
                        icon = R.drawable.delete,
                    )
                }
                else -> {
                    val action = mapAction(it) ?: return@mapNotNull null
                    RecordQuickActionsButtonViewData(
                        block = it,
                        text = recordQuickActionMapper.mapText(action),
                        icon = recordQuickActionMapper.mapIcon(action),
                    )
                }
            }
        }
    }

    private fun getAllowedButtons(
        extra: RecordQuickActionsParams,
        canContinue: Boolean,
        hasTags: Boolean,
    ): List<RecordQuickActionsButton> {
        return when (extra.type) {
            is Type.RecordTracked -> listOfNotNull(
                RecordQuickActionsButton.STATISTICS,
                RecordQuickActionsButton.DELETE,
                RecordQuickActionsButton.CONTINUE.takeIf { canContinue },
                RecordQuickActionsButton.REPEAT,
                RecordQuickActionsButton.DUPLICATE,
                RecordQuickActionsButton.MOVE,
                RecordQuickActionsButton.CHANGE_ACTIVITY,
                RecordQuickActionsButton.CHANGE_TAG.takeIf { hasTags },
            )
            is Type.RecordUntracked -> listOfNotNull(
                RecordQuickActionsButton.STATISTICS,
                RecordQuickActionsButton.MERGE,
                RecordQuickActionsButton.CHANGE_ACTIVITY,
            )
            is Type.RecordRunning -> listOfNotNull(
                RecordQuickActionsButton.STATISTICS,
                RecordQuickActionsButton.DELETE,
                RecordQuickActionsButton.STOP,
                RecordQuickActionsButton.CHANGE_ACTIVITY,
                RecordQuickActionsButton.CHANGE_TAG.takeIf { hasTags },
            )
            null -> emptyList()
        }
    }

    private fun applyWidth(
        buttons: List<ViewHolderType>,
    ): List<ViewHolderType> {
        val bigButtonsCount = buttons
            .count { it is RecordQuickActionsButtonBigViewData }
        val bigButtonLastIndex = buttons
            .indexOfLast { it is RecordQuickActionsButtonBigViewData }
        val smallButtonsCount = buttons
            .count { it is RecordQuickActionsButtonViewData }
        val smallButtonLastIndex = buttons
            .indexOfLast { it is RecordQuickActionsButtonViewData }

        return buttons.mapIndexed { index, button ->
            val isBigButtonFullWidth = bigButtonsCount % 2 != 0 && index == bigButtonLastIndex
            val isSmallButtonFullWidth = smallButtonsCount % 2 != 0 && index == smallButtonLastIndex
            when {
                button is RecordQuickActionsButtonBigViewData && isBigButtonFullWidth -> {
                    button.copy(width = RecordQuickActionsWidthHolder.Width.Full)
                }
                button is RecordQuickActionsButtonViewData && isSmallButtonFullWidth -> {
                    button.copy(width = RecordQuickActionsWidthHolder.Width.Full)
                }
                else -> button
            }
        }
    }

    private fun mapAction(
        action: RecordQuickActionsButton,
    ): RecordQuickAction? {
        return when (action) {
            RecordQuickActionsButton.STATISTICS -> null
            RecordQuickActionsButton.DELETE -> null
            RecordQuickActionsButton.CONTINUE -> RecordQuickAction.CONTINUE
            RecordQuickActionsButton.REPEAT -> RecordQuickAction.REPEAT
            RecordQuickActionsButton.DUPLICATE -> RecordQuickAction.DUPLICATE
            RecordQuickActionsButton.MOVE -> RecordQuickAction.MOVE
            RecordQuickActionsButton.MERGE -> RecordQuickAction.MERGE
            RecordQuickActionsButton.STOP -> RecordQuickAction.STOP
            RecordQuickActionsButton.CHANGE_ACTIVITY -> RecordQuickAction.CHANGE_ACTIVITY
            RecordQuickActionsButton.CHANGE_TAG -> RecordQuickAction.CHANGE_TAG
        }
    }

    private fun mapHint(
        allowedButtons: List<RecordQuickActionsButton>,
        isDarkTheme: Boolean,
    ): CharSequence {
        val builder = SpannableStringBuilder()

        val iconColor = resourceRepo.getThemedAttr(R.attr.appTextHintColor, isDarkTheme)
        val imageTag = resourceRepo.getString(R.string.image_tag)
        var needDividers = false

        allowedButtons.mapNotNull { button ->
            val action = mapAction(button) ?: return@mapNotNull null
            val hint = recordQuickActionMapper.mapHint(action) ?: return@mapNotNull null
            val name = recordQuickActionMapper.mapText(action)
            val icon = recordQuickActionMapper.mapIcon(action)
                .let(resourceRepo::getDrawable)
                ?.mutate()
                ?.apply { setTint(iconColor) }

            if (needDividers) builder.appendLine().appendLine()
            if (icon != null) {
                builder.image(
                    drawable = icon,
                    sizeDp = 16,
                    isCentered = true,
                    builderAction = { append(imageTag) },
                )
                builder.append(" ")
            }
            builder.bold { append(name) }
            builder.appendLine()
            builder.append(hint)
            needDividers = true
        }

        return builder
    }
}