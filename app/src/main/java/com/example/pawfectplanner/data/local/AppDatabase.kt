package com.example.pawfectplanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pawfectplanner.data.local.converters.LocalDateConverter
import com.example.pawfectplanner.data.model.Pet
import com.example.pawfectplanner.data.model.Task

@Database(entities = [Pet::class, Task::class], version = 1, exportSchema = false)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun taskDao(): TaskDao
}
