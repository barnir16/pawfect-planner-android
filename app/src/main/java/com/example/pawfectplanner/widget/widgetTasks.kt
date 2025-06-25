package com.example.pawfectplanner.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.pawfectplanner.R

/**
 * Implementation of App Widget functionality.
 */
class widget_tasks : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        // Handle custom actions if needed
        when (intent.action) {
            "com.example.pawfectplanner.ACTION_TASK_UPDATED" -> {
                val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                if (appWidgetIds != null) {
                    // Force update all widgets
                    onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds)
                }
            }
            "com.example.pawfectplanner.ACTION_REFRESH_WIDGET" -> {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    // Force refresh this specific widget
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.widget_tasks)
    
    // Set up the RemoteViews adapter for the ListView
    val intent = Intent(context, TaskWidgetService::class.java)
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    views.setRemoteAdapter(R.id.widget_task_list, intent)
    
    // Set empty view
    views.setEmptyView(R.id.widget_task_list, R.id.widget_empty_view)
    
    // Set up refresh button click
    val refreshIntent = Intent(context, widget_tasks::class.java).apply {
        action = "com.example.pawfectplanner.ACTION_REFRESH_WIDGET"
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }
    val refreshPendingIntent = android.app.PendingIntent.getBroadcast(
        context,
        appWidgetId,
        refreshIntent,
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
    
    // Force refresh the data in the ListView using the modern approach
    appWidgetManager.notifyAppWidgetViewDataChanged(intArrayOf(appWidgetId), R.id.widget_task_list)
}