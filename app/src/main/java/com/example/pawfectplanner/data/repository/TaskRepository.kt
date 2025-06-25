package com.example.pawfectplanner.data.repository

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.pawfectplanner.R
import com.example.pawfectplanner.data.local.TaskDao
import com.example.pawfectplanner.data.model.Task
import com.example.pawfectplanner.widget.widget_tasks
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val dao: TaskDao,
    private val context: Context
) {
    val allTasks: Flow<List<Task>> = dao.getAllTasks()
    
    suspend fun insert(task: Task) {
        dao.insert(task)
        updateWidgets()
    }
    
    suspend fun update(task: Task) {
        dao.update(task)
        updateWidgets()
    }
    
    suspend fun delete(task: Task) {
        dao.delete(task)
        updateWidgets()
    }
    
    private fun updateWidgets() {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, widget_tasks::class.java)
            )
            
            if (appWidgetIds.isNotEmpty()) {
                // Send broadcast to update widgets
                val intent = Intent(context, widget_tasks::class.java).apply {
                    action = "com.example.pawfectplanner.ACTION_TASK_UPDATED"
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
                
                // Also force update immediately using the modern approach
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_task_list)
            }
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        }
    }
}
