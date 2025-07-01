package com.example.pawfectplanner.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.pawfectplanner.ui.MainActivity
import com.example.pawfectplanner.R
import com.example.pawfectplanner.data.model.Task
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit

class TaskRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var tasks: List<Task> = emptyList()

    override fun onCreate() {
        tasks = loadTasks(context)
    }

    override fun onDataSetChanged() {
        tasks = loadTasks(context)
    }

    override fun onDestroy() {
        tasks = emptyList()
    }

    override fun getCount() = tasks.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_task_item)
        
        if (position < tasks.size) {
            val task = tasks[position]
            views.setTextViewText(R.id.widget_task_item_text, task.title)
            
            val timeRemaining = calculateTimeRemaining(task.dateTime)
            views.setTextViewText(R.id.widget_task_item_time, timeRemaining)

            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("task_id", task.id)
                putExtra("open_tasks", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                task.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_task_item_container, pendingIntent)
        }
        
        return views
    }

    override fun getLoadingView() = null
    override fun getViewTypeCount() = 1
    override fun getItemId(position: Int) = position.toLong()
    override fun hasStableIds() = true

    private fun loadTasks(context: Context): List<Task> {
        return try {
            val db = androidx.room.Room.databaseBuilder(
                context.applicationContext,
                com.example.pawfectplanner.data.local.AppDatabase::class.java,
                "pawfect_planner_database"
            ).allowMainThreadQueries().build()
            
            db.taskDao().getAllTasksSync().sortedBy { it.dateTime }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun calculateTimeRemaining(taskDateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        
        if (taskDateTime.isBefore(now)) {
            return context.getString(R.string.time_overdue)
        }
        
        val days = ChronoUnit.DAYS.between(now, taskDateTime)
        val hours = ChronoUnit.HOURS.between(now, taskDateTime)
        val minutes = ChronoUnit.MINUTES.between(now, taskDateTime)
        
        return when {
            days > 0 -> context.getString(R.string.time_days, days)
            hours > 0 -> context.getString(R.string.time_hours, hours)
            minutes > 0 -> context.getString(R.string.time_minutes, minutes)
            else -> context.getString(R.string.time_now)
        }
    }
}