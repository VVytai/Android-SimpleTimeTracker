package com.example.util.simpletimetracker.domain.recordShortcut.model

import com.example.util.simpletimetracker.domain.record.model.RecordBase

data class RecordShortcut(
    val id: Long = 0,
    val target: Target,
) {

    sealed interface Target {
        data class Record(
            val typeId: Long,
            val comment: String,
            val tags: List<RecordBase.Tag>,
        ) : Target

        data class Setting(
            val action: SettingAction,
        ) : Target
    }

    enum class SettingAction {
        Multitasking,
        RetroactiveMode,
    }
}
