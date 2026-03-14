package com.example.util.simpletimetracker.feature_shortcuts.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_shortcuts.interactor.ShortcutsViewDataInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShortcutsViewModel @Inject constructor(
    private val shortcutsViewDataInteractor: ShortcutsViewDataInteractor,
) : BaseViewModel() {

    val viewData: LiveData<List<ViewHolderType>> by lazySuspend {
        listOf(LoaderViewData()).also { updateViewData() }
    }

    private var navBarHeightDp: Int = 0

    fun onChangeInsets(navBarHeight: Int) {
        if (navBarHeightDp != navBarHeight) {
            navBarHeightDp = navBarHeight
            updateViewData()
        }
    }

    private fun updateViewData() = viewModelScope.launch {
        val data = shortcutsViewDataInteractor.getViewData(navBarHeightDp)
        delayLoad()
        viewData.set(data)
    }
}
