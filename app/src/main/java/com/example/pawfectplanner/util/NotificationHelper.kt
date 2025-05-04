package com.example.pawfectplanner.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.pawfectplanner.data.model.Task
import org.threeten.bp.ZoneId

object NotificationHelper {
    fun schedule(context: Context, task: Task) {
        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("taskId", task.id)
            putExtra("title", task.title)
            putExtra("description", task.description)
        }
        val pi = PendingIntent.getBroadcast(
            context, task.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = task.dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (task.repeatInterval != null && task.repeatUnit != null) {
            val intervalMs = when (task.repeatUnit) {
                "Minutes" -> task.repeatInterval * 60_000L
                "Hours"   -> task.repeatInterval * 3_600_000L
                "Days"    -> task.repeatInterval * 86_400_000L
                "Weeks"   -> task.repeatInterval * 604_800_000L
                "Months"  -> task.repeatInterval * 2_592_000_000L
                "Years"   -> task.repeatInterval * 31_536_000_000L
                else      -> null
            }
            if (intervalMs != null) {
                mgr.setRepeating(AlarmManager.RTC_WAKEUP, triggerAt, intervalMs, pi)
                return
            }
        }
        mgr.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }

    fun cancel(context: Context, taskId: Int) {
        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, taskId, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        mgr.cancel(pi)
    }
}
