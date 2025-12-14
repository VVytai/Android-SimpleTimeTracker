package com.example.util.simpletimetracker.core.delegates.dateSelector.viewModelDelegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.dateSelector.mapper.DateSelectorMapper
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.InfiniteRecyclerAdapter
import javax.inject.Inject

class DateSelectorViewModelDelegate @Inject constructor(
    val dataProvider: DateSelectorMapper,
    private val prefsInteractor: PrefsInteractor,
) : ViewModelDelegate() {

    val dateScrollPosition: LiveData<Int> = SingleLiveEvent<Int>()
    val updateDatesViewData: LiveData<Unit> = SingleLiveEvent<Unit>()
    val borderShadowsVisibility: LiveData<Boolean> = MutableLiveData()

    private var parent: Parent? = null

    fun attach(parent: Parent) {
        this.parent = parent
    }

    suspend fun initialize(position: Int) {
        setup()
        updatePosition(position)
    }

    suspend fun setup() {
        setupDatesSelector()
    }

    fun onDateClick(item: InfiniteRecyclerAdapter.Data) {
        if (parent?.currentPosition == item.position) {
            throttle { parent?.onDateClick() }.invoke()
        } else {
            parent?.updatePosition(item.position)
        }
    }

    fun onDateLongClick(item: InfiniteRecyclerAdapter.Data) {
        if (parent?.currentPosition == item.position) {
            parent?.updatePosition(0)
        } else {
            onDateClick(item)
        }
    }

    fun onScrolledToDate(position: Int) {
        if (position != parent?.currentPosition) {
            parent?.updatePosition(position)
        }
    }

    fun updatePosition(shift: Int) {
        dataProvider.currentSelectedPosition = shift
        updateDatesViewData.set(Unit)
        dateScrollPosition.set(shift)
    }

    private suspend fun setupDatesSelector() {
        val setupData = DateSelectorMapper.SetupData(
            type = parent?.getSetupData() ?: return,
            startOfDayShift = prefsInteractor.getStartOfDayShift(),
            firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
        )
        dataProvider.setup(setupData)

        // Updates after setup.
        val shadowsVisibility = dataProvider.getCount() ==
            InfiniteRecyclerAdapter.DataProvider.Count.Infinite
        borderShadowsVisibility.set(shadowsVisibility)
    }

    interface Parent {
        val currentPosition: Int

        fun onDateClick()
        fun updatePosition(newPosition: Int)
        suspend fun getSetupData(): DateSelectorMapper.SetupData.Type
    }
}