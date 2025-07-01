package com.example.pawfectplanner.di

import android.content.Context
import com.example.pawfectplanner.data.local.PetDao
import com.example.pawfectplanner.data.local.TaskDao
import com.example.pawfectplanner.data.repository.BreedsRepository
import com.example.pawfectplanner.data.repository.GeminiRepository
import com.example.pawfectplanner.data.repository.PetRepository
import com.example.pawfectplanner.data.repository.TaskRepository
import com.example.pawfectplanner.data.repository.VaccineLocalizedRepository
import com.example.pawfectplanner.network.BreedsCatApiService
import com.example.pawfectplanner.network.BreedsDogApiService
import com.example.pawfectplanner.network.GeminiApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providePetRepository(petDao: PetDao): PetRepository {
        return PetRepository(petDao)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao,
        @ApplicationContext context: Context
    ): TaskRepository {
        return TaskRepository(taskDao, context)
    }

    @Provides
    @Singleton
    fun provideBreedsRepository(
        dogApiService: BreedsDogApiService,
        catApiService: BreedsCatApiService
    ): BreedsRepository {
        return BreedsRepository(dogApiService, catApiService)
    }

    @Provides
    @Singleton
    fun provideGeminiRepository(
        geminiApiService: GeminiApiService,
        @ApplicationContext context: Context
    ): GeminiRepository {
        return GeminiRepository(geminiApiService, context)
    }

    @Provides
    @Singleton
    fun provideVaccineLocalizedRepository(
        @ApplicationContext context: Context
    ): VaccineLocalizedRepository {
        return VaccineLocalizedRepository(context)
    }
} 