package com.example.pawfectplanner

import android.app.Application
import androidx.room.Room
import com.example.pawfectplanner.data.local.AppDatabase

class PawfectPlannerApplication : Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "pawfect_planner_db"
        ).build()
    }
}
