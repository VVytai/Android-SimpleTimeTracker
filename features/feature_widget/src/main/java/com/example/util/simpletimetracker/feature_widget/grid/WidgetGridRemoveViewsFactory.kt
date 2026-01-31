package com.example.util.simpletimetracker.feature_widget.grid

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.util.simpletimetracker.core.extension.allowVmViolations
import com.example.util.simpletimetracker.core.interactor.CompleteTypesStateInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_views.ColorUtils
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.RecordTypeView
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_views.extension.pxToDp
import com.example.util.simpletimetracker.feature_views.extension.setAllMargins
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.grid.WidgetGridProvider.Companion.ITEM_CLICK_ACTION
import com.example.util.simpletimetracker.feature_widget.grid.WidgetGridProvider.Companion.TYPE_ID_EXTRA
import com.example.util.simpletimetracker.feature_widget.utils.setRecordTypeTimers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class WidgetGridRemoveViewsFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val resourceRepo: ResourceRepo,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordInteractor: RecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val completeTypesStateInteractor: CompleteTypesStateInteractor,
) : RemoteViewsService.RemoteViewsFactory {

    var intent: Intent? = null

    private var recordTypes: List<RecordType> = emptyList()
    private var runningRecords: List<RunningRecord> = emptyList()
    private var prevRecords: List<Record> = emptyList()
    private var preparedView: RecordTypeView? = null
    private var isDarkTheme: Boolean = false
    private var backgroundTransparency: Long = 0

    override fun onCreate() {
        // TODO
    }

    override fun onDataSetChanged() {
        runBlocking {
            recordTypes = recordTypeInteractor.getAll()
            runningRecords = runningRecordInteractor.getAll()
            isDarkTheme = prefsInteractor.getDarkMode()
            backgroundTransparency = prefsInteractor.getWidgetBackgroundTransparencyPercent()
            prevRecords = if (prefsInteractor.getRetroactiveTrackingMode()) {
                recordInteractor.getAllPrev(timeStarted = System.currentTimeMillis())
            } else {
                emptyList()
            }
        }
    }

    override fun onDestroy() {
        // TODO
    }

    override fun getCount(): Int {
        val columnCount = getColumnCount()
        val reminder = if (recordTypes.size % columnCount > 0) 1 else 0
        return (recordTypes.size / columnCount) + reminder
    }

    // TODO WIDGET:
    // TODO check retroactive
    // TODO add default duration support
    // TODO add goals
    // TODO add clicks
    // TODO add tag selection
    // TODO add settings activity to select number of cards and filter some activities
    // TODO add repeat
    override fun getViewAt(position: Int): RemoteViews? {
        fun getRunningRecord(typeId: Long): RunningRecord? {
            return runningRecords.firstOrNull { record -> record.id == typeId }
        }

        fun getPrevRecord(typeId: Long): Record? {
            return prevRecords.firstOrNull { record -> record.typeId == typeId }
        }

        val appWidgetId: Int = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
        val columnCount = getColumnCount()
        val firstItemInRowIndex = position * columnCount

        val typesInThisRow = (firstItemInRowIndex until firstItemInRowIndex + columnCount).mapNotNull {
            val recordType = recordTypes.getOrNull(it) ?: return@mapNotNull null
            val runningRecord = getRunningRecord(recordType.id)
            val prevRecord = getPrevRecord(recordType.id)

            val isColored = when {
                runningRecord != null -> true
                prevRecord != null -> true
                else -> false
            }
            val bitmap = prepareView(
                context = context,
                recordType = recordType,
                isColored = isColored,
                checkState = GoalCheckmarkView.CheckState.HIDDEN,
                isComplete = recordType.id in completeTypesStateInteractor.widgetTypeIds,
            ).also { view ->
                measureView2(context, options, columnCount, view)
            }.getBitmapFromView()
            recordType to bitmap
        }

        if (typesInThisRow.isEmpty()) return null
        val needEmptyViews = typesInThisRow.size < columnCount

        val views = RemoteViews(context.packageName, R.layout.widget_items_layout)
        views.removeAllViews(R.id.containerWidgets)

        if (needEmptyViews) views.addContainer()

        typesInThisRow.forEach { (recordType, bitmap) ->
            val runningRecord = getRunningRecord(recordType.id)
            val prevRecord = getPrevRecord(recordType.id)

            val item = RemoteViews(context.packageName, R.layout.widget_layout)
            setRecordTypeTimers(runningRecord, prevRecord, item)
            item.setImageViewBitmap(R.id.ivWidgetBackground, bitmap)

            val clickPendingIntent = getPendingIntent(context, appWidgetId, recordType.id)
            item.setOnClickPendingIntent(R.id.btnWidget, clickPendingIntent)
            // TODO WIDGET fillInIntent not working
            // val fillInIntent = Intent()
            // fillInIntent.putExtra(TYPE_ID_EXTRA, recordType.id)
            // item.setOnClickFillInIntent(R.id.btnWidget, fillInIntent)

            views.addContainer(item)
        }

        if (needEmptyViews) views.addContainer()

        return views
    }

    override fun getLoadingView(): RemoteViews? {
        return null // TODO
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        val firstItemInRowIndex = position * getColumnCount()
        return recordTypes.getOrNull(firstItemInRowIndex)?.id.orZero()
    }

    override fun hasStableIds(): Boolean {
        return true // TODO
    }

    private fun RemoteViews.addContainer(item: RemoteViews? = null) {
        val itemContainer = RemoteViews(context.packageName, R.layout.widget_item_layout)
        item?.let { itemContainer.addView(R.id.containerWidgets, it) }
        this.addView(R.id.containerWidgets, itemContainer)
    }

    private fun prepareView(
        context: Context,
        recordType: RecordType,
        isColored: Boolean,
        checkState: GoalCheckmarkView.CheckState,
        isComplete: Boolean,
    ): View {
        val icon = recordType.icon

        val name = recordType.name

        val textColor = if (isColored) {
            resourceRepo.getColor(R.color.colorIcon)
        } else {
            resourceRepo.getColor(R.color.widget_universal_empty_color)
        }

        val color = if (isColored) {
            colorMapper.mapToColorInt(recordType.color, isDarkTheme)
        } else {
            ColorUtils.changeAlpha(
                color = resourceRepo.getColor(R.color.widget_universal_background_color),
                alpha = 1f - backgroundTransparency / 100f,
            )
        }

        val view = getView(context).apply {
            (parent as? ViewGroup)?.removeAllViews()
            itemIcon = iconMapper.mapIcon(icon)
            itemName = name
            itemIconColor = textColor
            itemColor = color
            itemCheckState = checkState
            itemCompleteIsAnimated = false
            itemIsComplete = isComplete
        }

        return view
    }

    private fun measureView2(
        context: Context,
        options: Bundle?,
        columnCount: Int,
        view: View,
    ) {
        val defaultWidth = context.resources.getDimensionPixelSize(R.dimen.record_type_card_width)
        val defaultHeight = context.resources.getDimensionPixelSize(R.dimen.record_type_card_height)

        val width = options?.getInt(
            AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
            defaultWidth.pxToDp(),
        )?.dpToPx().takeUnless { it == 0 } ?: defaultWidth
        val height = options?.getInt(
            AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,
            defaultHeight.pxToDp(),
        )?.dpToPx().takeUnless { it == 0 } ?: defaultHeight

        view.measureExactly(width = width / columnCount, height = defaultHeight)
    }

    private fun getView(context: Context): RecordTypeView {
        preparedView?.let { return it }

        val view = allowVmViolations {
            RecordTypeView(ContextThemeWrapper(context, R.style.AppTheme))
        }.apply {
            getContainer().radius = resources
                .getDimensionPixelOffset(R.dimen.widget_universal_corner_radius).toFloat()
            getContainer().cardElevation = 0f
            getContainer().useCompatPadding = false
            getCheckmarkOutline().setAllMargins(4)
        }
        preparedView = view

        return view
    }

    private fun getColumnCount(): Int {
        return 5
    }

    private fun getPendingIntent(
        context: Context,
        appWidgetId: Int,
        recordTypeId: Long,
    ): PendingIntent? {
        val clickIntent = Intent(context, WidgetGridProvider::class.java)
        clickIntent.action = ITEM_CLICK_ACTION
        clickIntent.putExtra(TYPE_ID_EXTRA, recordTypeId)
        return PendingIntent.getBroadcast(
            context,
            RequestCode(
                appWidgetId = appWidgetId,
                recordTypeId = recordTypeId
            ).hashCode(),
            clickIntent,
            PendingIntents.getFlags()
        )
    }

    private data class RequestCode(
        val appWidgetId: Int,
        val recordTypeId: Long,
    )
}