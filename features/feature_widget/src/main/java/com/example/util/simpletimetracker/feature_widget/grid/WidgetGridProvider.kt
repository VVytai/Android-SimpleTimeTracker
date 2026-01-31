package com.example.util.simpletimetracker.feature_widget.grid

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.example.util.simpletimetracker.core.extension.allowDiskRead
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.widget.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.widget.model.WidgetType
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.common.WidgetTypeClickManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class WidgetGridProvider : AppWidgetProvider() {

    @Inject
    lateinit var widgetTypeClickManager: WidgetTypeClickManager

    @Inject
    lateinit var widgetInteractor: WidgetInteractor

    // TODO WIDGET use WidgetViewsHolder
    // TODO WIDGET add preview
    // TODO WIDGET translate strings
    // TODO WIDGET show tag selection
    // TODO Lunch activity is not ticking

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == ITEM_CLICK_ACTION) onClick(context, intent)
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?,
    ) {
        appWidgetIds?.forEach { widgetId ->
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    private fun updateAppWidget(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
    ) {
        if (context == null || appWidgetManager == null) return

        val serviceIntent = Intent(context, WidgetGridRemoteViewsService::class.java)
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        val views = RemoteViews(context.packageName, R.layout.widgets_layout)
        views.setRemoteAdapter(R.id.listWidgets, serviceIntent)
        // TODO WIDGET set empty view

        // TODO WIDGET fillInIntent not working
        // val clickIntent = Intent(context, WidgetGridProvider::class.java)
        // clickIntent.action = ITEM_CLICK_ACTION
        // val clickPendingIntent = PendingIntent.getBroadcast(
        //     context,
        //     0,
        //     clickIntent,
        //     PendingIntents.getFlags()
        // )
        // views.setPendingIntentTemplate(R.id.listWidgets, clickPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
        // TODO WIDGET
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listWidgets)
    }

    private fun onClick(
        context: Context?,
        intent: Intent,
    ) {
        allowDiskRead { MainScope() }.launch {
            widgetTypeClickManager.onClick(
                context = context,
                recordTypeId = intent.getLongExtra(TYPE_ID_EXTRA, 0),
                onWidgetUpdate = { widgetInteractor.updateWidgets(WidgetType.GRID) }
            )
        }
    }

    companion object {
        // TODO WIDGET
        const val TYPE_ID_EXTRA =
            "com.example.util.simpletimetracker.feature_widget.grid.typeIdExtra"
        const val ITEM_CLICK_ACTION =
            "com.yourdomain.app.ITEM_CLICK_ACTION"
    }
}