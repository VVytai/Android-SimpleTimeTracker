package com.example.util.simpletimetracker.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

abstract class BaseViewModel : ViewModel(), ScopeHolder, Throttler {

    override var throttleJob: Job? = null

    override fun getScope(): CoroutineScope {
        return viewModelScope
    }
}