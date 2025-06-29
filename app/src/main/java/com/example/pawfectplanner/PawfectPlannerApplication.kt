package com.example.pawfectplanner

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.Room
import com.example.pawfectplanner.data.local.AppDatabase
import com.example.pawfectplanner.util.ApiKeyManager
import com.example.pawfectplanner.util.RemoteConfigManager
import com.example.pawfectplanner.util.LocaleHelper
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PawfectPlannerApplication : Application() {

    companion object {
        lateinit var instance: PawfectPlannerApplication
            private set
    }

    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        instance = this

        FirebaseApp.initializeApp(this)
        RemoteConfigManager.init()

        CoroutineScope(Dispatchers.IO).launch {
            RemoteConfigManager.fetchAndActivate()
            ApiKeyManager.petsApiKey = RemoteConfigManager.getPetsApiKey()
            ApiKeyManager.geminiApiKey = RemoteConfigManager.getGeminiApiKey()
        }

        AndroidThreeTen.init(this)
        LocaleHelper.initializeDarkMode(this)

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
