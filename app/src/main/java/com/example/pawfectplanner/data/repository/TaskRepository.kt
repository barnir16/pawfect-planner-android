package com.example.pawfectplanner.data.repository

import com.example.pawfectplanner.data.local.TaskDao
import com.example.pawfectplanner.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {
    val allTasks: Flow<List<Task>> = dao.getAllTasks()
    suspend fun insert(task: Task) = dao.insert(task)
    suspend fun update(task: Task) = dao.update(task)
    suspend fun delete(task: Task) = dao.delete(task)
}
