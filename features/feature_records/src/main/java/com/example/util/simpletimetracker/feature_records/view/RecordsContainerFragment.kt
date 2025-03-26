package com.example.util.simpletimetracker.feature_records.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.dialog.OptionsListDialogListener
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.core.sharedViewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.view.SafeFragmentStateAdapter
import com.example.util.simpletimetracker.feature_base_adapter.optionsList.OptionsListViewData
import com.example.util.simpletimetracker.feature_records.adapter.RecordsContainerAdapter
import com.example.util.simpletimetracker.feature_records.model.RecordsContainerPosition
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsContainerViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_records.databinding.RecordsContainerFragmentBinding as Binding

@AndroidEntryPoint
class RecordsContainerFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener,
    OptionsListDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.DoNotApply

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    @Inject
    lateinit var mainTabsViewModelFactory: BaseViewModelFactory<MainTabsViewModel>

    @Inject
    lateinit var router: Router

    private val viewModel: RecordsContainerViewModel by viewModels()
    private val removeRecordViewModel: RemoveRecordViewModel by activityViewModels(
        factoryProducer = { removeRecordViewModelFactory },
    )
    private val mainTabsViewModel: MainTabsViewModel by activityViewModels(
        factoryProducer = { mainTabsViewModelFactory },
    )

    override fun initUi(): Unit = with(binding) {
        pagerRecordsContainer.apply {
            adapter = SafeFragmentStateAdapter(
                RecordsContainerAdapter(this@RecordsContainerFragment),
            )
            offscreenPageLimit = 1
            isUserInputEnabled = false
        }
    }

    override fun initUx() = with(binding) {
        btnRecordAdd.setOnClick(throttle(viewModel::onOptionsClick))
        btnRecordsContainerPrevious.setOnClick(viewModel::onPreviousClick)
        btnRecordsContainerNext.setOnClick(viewModel::onNextClick)
        btnRecordsContainerToday.setOnClick(viewModel::onTodayClick)
        btnRecordsContainerToday.setOnLongClick(viewModel::onTodayLongClick)
    }

    override fun initViewModel() {
        with(viewModel) {
            title.observe(::updateTitle)
            position.observe(::setPosition)
        }
        with(removeRecordViewModel) {
            message.observe(::showMessage)
        }
        with(mainTabsViewModel) {
            isNavBatAtTheBottom.observe(::updateInsetConfiguration)
        }
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    override fun onOptionsItemClick(item: OptionsListViewData) {
        viewModel.onOptionsItemClick(item)
    }

    private fun updateTitle(title: String) {
        binding.btnRecordsContainerToday.text = title
    }

    private fun showMessage(message: SnackBarParams?) {
        if (message != null && message.tag == SnackBarParams.TAG.RECORD_DELETE) {
            router.show(message, binding.btnRecordAdd)
            removeRecordViewModel.onMessageShown()
        }
    }

    private fun setPosition(data: RecordsContainerPosition) = with(binding) {
        pagerRecordsContainer.setCurrentItem(
            data.position + RecordsContainerAdapter.FIRST,
            data.animate && viewPagerSmoothScroll,
        )
    }

    private fun updateInsetConfiguration(isNavBatAtTheBottom: Boolean) {
        insetConfiguration = if (isNavBatAtTheBottom) {
            InsetConfiguration.DoNotApply
        } else {
            InsetConfiguration.ApplyToView { binding.root }
        }
        initInsets()
    }

    companion object {
        @VisibleForTesting
        var viewPagerSmoothScroll: Boolean = true

        fun newInstance() = RecordsContainerFragment()
    }
}