package com.example.pawfectplanner

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.pawfectplanner.data.local.AppDatabase
import com.example.pawfectplanner.util.ApiKeyManager
import com.example.pawfectplanner.util.LocaleHelper
import com.example.pawfectplanner.util.RemoteConfigManager
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class PawfectPlannerApplication : Application() {

    companion object {
        lateinit var instance: PawfectPlannerApplication
            private set
    }

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
            val name = getString(R.string.notification_channel_tasks_name)
            val desc = getString(R.string.notification_channel_tasks_description)
            val channel = NotificationChannel(
                "task_reminders",
                name,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = desc
            }
            nm?.createNotificationChannel(channel)
        }
    }
}
