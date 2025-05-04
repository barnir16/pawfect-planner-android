package com.example.pawfectplanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pawfectplanner.data.local.converters.Converters
import com.example.pawfectplanner.data.model.Pet
import com.example.pawfectplanner.data.model.Task

@Database(entities = [Pet::class, Task::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun taskDao(): TaskDao
}
