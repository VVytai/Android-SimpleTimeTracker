package com.example.util.simpletimetracker.domain.record.model

data class RecordDataSelectionDialogResult(
    val fields: List<Field>,
    val requiredTagValueSelectionTagIds: List<Long> = emptyList(),
) {
    sealed interface Field {
        object Tags : Field
        object Comment : Field
    }
}