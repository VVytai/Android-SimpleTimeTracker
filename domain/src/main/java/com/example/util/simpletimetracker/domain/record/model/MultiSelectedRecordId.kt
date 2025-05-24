package com.example.util.simpletimetracker.domain.record.model

sealed interface MultiSelectedRecordId {
    data class Tracked(val id: Long) : MultiSelectedRecordId
    data class Untracked(val timeStartedTimestamp: Long) : MultiSelectedRecordId
    data class Running(val id: Long) : MultiSelectedRecordId
}