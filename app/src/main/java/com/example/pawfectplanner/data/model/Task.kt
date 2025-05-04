package com.example.pawfectplanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDateTime

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val dateTime: LocalDateTime,
    val repeatInterval: Int? = null,
    val repeatUnit: String? = null,
    val petIds: List<Int> = emptyList()
)
