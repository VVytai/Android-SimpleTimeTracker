package com.example.util.simpletimetracker.domain.recordShortcut.model

import com.example.util.simpletimetracker.domain.record.model.RecordBase

data class RecordShortcut(
    val id: Long = 0,
    val typeId: Long,
    val comment: String,
    val tags: List<RecordBase.Tag>,
)