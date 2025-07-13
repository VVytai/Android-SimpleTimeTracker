package com.example.util.simpletimetracker.domain.notifications.interactor

interface NotificationActivitySwitchInteractor {

    suspend fun updateNotification(
        typesShift: Int = 0,
        tagsShift: Int = 0,
        selectedTypeId: Long = 0,
        selectedTagId: Long = 0,
        selectedTagValue: String? = null,
    )
}