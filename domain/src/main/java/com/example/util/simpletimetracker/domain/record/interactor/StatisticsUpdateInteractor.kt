package com.example.util.simpletimetracker.domain.record.interactor

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsUpdateInteractor @Inject constructor() {

    val shareClicked: SharedFlow<Unit> get() = _shareClicked.asSharedFlow()
    private val _shareClicked = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val filterClicked: SharedFlow<Unit> get() = _filterClicked.asSharedFlow()
    private val _filterClicked = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    suspend fun sendShareClicked() {
        _shareClicked.emit(Unit)
    }

    suspend fun sendFilterClicked() {
        _filterClicked.emit(Unit)
    }
}