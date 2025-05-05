package com.example.pawfectplanner

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.Room
import com.example.pawfectplanner.data.local.AppDatabase
import com.jakewharton.threetenabp.AndroidThreeTen

class PawfectPlannerApplication : Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(
                NotificationChannel(
                    "task_reminders",
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for your pet tasks"
                }
            )
        }

        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "pawfect_planner_db"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }
}
