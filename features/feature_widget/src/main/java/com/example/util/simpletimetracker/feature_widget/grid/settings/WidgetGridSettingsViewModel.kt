package com.example.util.simpletimetracker.feature_widget.grid.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.widget.interactor.WidgetInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetGridSettingsViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
) : ViewModel() {

    lateinit var extra: WidgetGridSettingsExtra

    val types: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initializeWidgetData()
                initial.value = listOf(LoaderViewData())
                initial.value = loadTypesViewData()
            }
            initial
        }
    }
    val handled: LiveData<Int> = MutableLiveData()

    private var recordTypesCache: List<RecordType>? = null
    private var filteredTypeIds: Set<Long> = emptySet()
    private var initialized: Boolean = false

    fun onRecordTypeClick(item: RecordTypeViewData) = viewModelScope.launch {
        filteredTypeIds = filteredTypeIds.toMutableSet().apply {
            if (item.id in this) remove(item.id) else add(item.id)
        }
        updateTypesViewData()
    }

    fun onShowAllClick() = viewModelScope.launch {
        filteredTypeIds = emptySet()
        updateTypesViewData()
    }

    fun onHideAllClick() = viewModelScope.launch {
        filteredTypeIds = getTypesCache().map(RecordType::id).toSet()
        updateTypesViewData()
    }

    fun onSaveClick() {
        viewModelScope.launch {
            prefsInteractor.setGridWidgetFilteredTypes(extra.widgetId, filteredTypeIds)
            widgetInteractor.updateGridWidget(extra.widgetId)
            (handled as MutableLiveData).value = extra.widgetId
        }
    }

    private suspend fun initializeWidgetData() {
        if (initialized) return
        filteredTypeIds = prefsInteractor.getGridWidgetFilteredTypes(extra.widgetId)
        initialized = true
    }

    private fun updateTypesViewData() = viewModelScope.launch {
        types.set(loadTypesViewData())
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val numberOfCards = prefsInteractor.getNumberOfCards()

        return getTypesCache()
            .map { type ->
                recordTypeViewDataMapper.mapFiltered(
                    recordType = type,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme,
                    isFiltered = type.id in filteredTypeIds,
                    checkState = GoalCheckmarkView.CheckState.HIDDEN,
                    isComplete = false,
                )
            }
            .takeUnless { it.isEmpty() }
            ?: recordTypeViewDataMapper.mapToEmpty()
    }

    private suspend fun getTypesCache(): List<RecordType> {
        return recordTypesCache ?: run {
            recordTypeInteractor.getAll()
                .filter { !it.hidden }
                .also { recordTypesCache = it }
        }
    }
}
