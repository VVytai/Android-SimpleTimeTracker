package com.example.util.simpletimetracker.feature_records.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.dialog.OptionsListDialogListener
import com.example.util.simpletimetracker.core.extension.changeDragSensitivity
import com.example.util.simpletimetracker.core.extension.horizontalSmoothScrollWithOffset
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.core.sharedViewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.view.SafeFragmentStateAdapter
import com.example.util.simpletimetracker.feature_base_adapter.InfiniteRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.dateSelector.createDateSelectorAdapterDelegate
import com.example.util.simpletimetracker.feature_records.adapter.RecordsContainerAdapter
import com.example.util.simpletimetracker.feature_records.model.RecordsContainerPosition
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsContainerViewModel
import com.example.util.simpletimetracker.feature_views.extension.addOnScrollListenerAdapter
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
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
    private val dateSelectorAdapter by lazy {
        InfiniteRecyclerAdapter(
            createDateSelectorAdapterDelegate(
                mapper = viewModel.dateSelectorMapper,
                onItemClick = viewModel::onDateClick,
            ),
        )
    }
    private val snapHelper by lazy { LinearSnapHelper() }
    private var scrollWasAlreadyRequested: Boolean = false

    override fun initUi(): Unit = with(binding) {
        pagerRecordsContainer.apply {
            adapter = SafeFragmentStateAdapter(
                RecordsContainerAdapter(this@RecordsContainerFragment),
            )
            offscreenPageLimit = 1
            isUserInputEnabled = false
        }
        rvDatesContainer.apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = RecyclerView.HORIZONTAL
            }
            adapter = dateSelectorAdapter
            snapHelper.attachToRecyclerView(this)
            doScrollToPosition(0)
            addOnScrollListenerAdapter(onScrollStateChanged = ::onDatesScrolled)
            changeDragSensitivity(0.1f)
        }
    }

    override fun initUx() = with(binding) {
        btnRecordsContainerAdd.setOnClick(throttle(viewModel::onOptionsLongClick))
        btnRecordsContainerOptions.setOnClick(throttle(viewModel::onOptionsClick))
        containerRecordsContainerCurrentDate.setOnClick(viewModel::onTodayClick)
        containerRecordsContainerCurrentDate.setOnLongClick(viewModel::onTodayLongClick)
    }

    override fun initViewModel() {
        with(viewModel) {
            title.observe(::updateTitle)
            position.observe(::setPosition)
            dateScrollPosition.observe(::doScrollToPosition)
            // TODO DATE do better?
            updateDatesViewData.observe { dateSelectorAdapter.notifyDataSetChanged() }
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

    override fun onOptionsItemClick(id: OptionsListParams.Item.Id) {
        viewModel.onOptionsItemClick(id)
    }

    private fun updateTitle(title: String) {
        binding.tvRecordsContainerCurrentDate.text = title
    }

    private fun showMessage(message: SnackBarParams?) {
        if (message != null && message.tag == SnackBarParams.TAG.RECORD_DELETE) {
            router.show(message, binding.btnRecordsContainerAdd)
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

    // TODO DATE Add calendar support
    // TODO DATE Add to statistics
    // TODO DATE rename ContainerRangeButton style
    // TODO DATE remove sharing title from records and stats?
    private fun doScrollToPosition(position: Int) {
        scrollWasAlreadyRequested = true
        val recycler = binding.rvDatesContainer
        val layoutManager = recycler.layoutManager as? LinearLayoutManager ?: return
        val actualPosition = position + InfiniteRecyclerAdapter.FIRST
        val firstPosition = layoutManager.findFirstVisibleItemPosition()
        val lastPosition = layoutManager.findLastVisibleItemPosition()
        val smoothScrollThreshold = 10

        // To long to scroll with animation if scroll distance is long,
        // in this case scrollToPosition closer, and than smoothScroll with animation.
        if ((firstPosition - actualPosition) > smoothScrollThreshold) {
            recycler.scrollToPosition(actualPosition + smoothScrollThreshold)
        } else if ((actualPosition - lastPosition) > smoothScrollThreshold) {
            recycler.scrollToPosition(actualPosition - smoothScrollThreshold)
        }

        recycler.post {
            recycler.horizontalSmoothScrollWithOffset(
                snapPreference = LinearSmoothScroller.SNAP_TO_END,
                position = actualPosition,
                calculateOffset = { recyclerView, view ->
                    // center of the recycler.
                    -recyclerView.width / 2 + view.width / 2
                },
            )
        }
    }

    private fun onDatesScrolled(
        recyclerView: RecyclerView,
        newState: Int,
    ) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            // Scroll already initiated from viewModel, no need to notify it,
            // otherwise back to today click will be canceled half way.
            if (!scrollWasAlreadyRequested) {
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                val snapView = snapHelper.findSnapView(layoutManager) ?: return
                val snapPosition = layoutManager?.getPosition(snapView) ?: return
                viewModel.onScrolledToDate(snapPosition - InfiniteRecyclerAdapter.FIRST)
            }
            scrollWasAlreadyRequested = false
        }
    }

    companion object {
        @VisibleForTesting
        var viewPagerSmoothScroll: Boolean = true

        fun newInstance() = RecordsContainerFragment()
    }
}