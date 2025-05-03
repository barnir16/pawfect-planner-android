package com.example.pawfectplanner

import android.app.Application
import androidx.room.Room
import com.example.pawfectplanner.data.local.AppDatabase
import com.jakewharton.threetenabp.AndroidThreeTen

class PawfectPlannerApplication : Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "pawfect_planner_db"
        ).build()
    }
}