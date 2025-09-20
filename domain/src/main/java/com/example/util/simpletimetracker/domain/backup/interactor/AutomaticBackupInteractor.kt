package com.example.util.simpletimetracker.domain.backup.interactor

interface AutomaticBackupInteractor {

    suspend fun schedule()

    fun cancel()

    fun onFinished()

    suspend fun backup()
}