package com.example.util.simpletimetracker.feature_widget.grid

import android.content.Intent
import android.widget.RemoteViewsService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WidgetGridRemoteViewsService : RemoteViewsService() {

    @Inject
    lateinit var widgetGridRemoveViewsFactory: WidgetGridRemoveViewsFactory

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        widgetGridRemoveViewsFactory.intent = intent
        return widgetGridRemoveViewsFactory
    }
}