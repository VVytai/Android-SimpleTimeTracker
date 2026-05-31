package com.example.util.simpletimetracker.feature_running_records.di

import com.example.util.simpletimetracker.feature_running_records.api.OnShortcutClickInteractor
import com.example.util.simpletimetracker.feature_running_records.api.RecordsShortcutsViewDataInteractor
import com.example.util.simpletimetracker.feature_running_records.interactor.OnShortcutClickInteractorImpl
import com.example.util.simpletimetracker.feature_running_records.interactor.RecordsShortcutsViewDataInteractorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RunningRecordsModule {

    @Binds
    fun bindRecordsShortcutsViewDataInteractor(impl: RecordsShortcutsViewDataInteractorImpl): RecordsShortcutsViewDataInteractor

    @Binds
    fun bindOnShortcutClickInteractor(impl: OnShortcutClickInteractorImpl): OnShortcutClickInteractor
}