package com.example.util.simpletimetracker.feature_settings.di

import com.example.util.simpletimetracker.feature_settings.api.OnSettingChangedInteractor
import com.example.util.simpletimetracker.feature_settings.api.SettingsCardOrderMapper
import com.example.util.simpletimetracker.feature_settings.api.SettingsOrderChangeInteractor
import com.example.util.simpletimetracker.feature_settings.interactor.OnSettingChangedInteractorImpl
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsOrderChangeInteractorImpl
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface SettingsModule {

    @Binds
    fun bindSettingsCardOrderMapper(impl: SettingsMapper): SettingsCardOrderMapper

    @Binds
    fun bindOnSettingChangedInteractor(impl: OnSettingChangedInteractorImpl): OnSettingChangedInteractor

    @Binds
    fun bindSettingsOrderChangeInteractor(impl: SettingsOrderChangeInteractorImpl): SettingsOrderChangeInteractor
}