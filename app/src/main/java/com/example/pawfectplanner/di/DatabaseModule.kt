package com.example.pawfectplanner.di

import android.content.Context
import androidx.room.Room
import com.example.pawfectplanner.data.local.AppDatabase
import com.example.pawfectplanner.data.local.PetDao
import com.example.pawfectplanner.data.local.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pawfect_planner_database"
        ).build()
    }

    @Provides
    @Singleton
    fun providePetDao(database: AppDatabase): PetDao {
        return database.petDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }
} 