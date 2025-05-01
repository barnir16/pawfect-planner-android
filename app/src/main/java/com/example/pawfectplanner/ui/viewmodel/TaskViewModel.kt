package com.example.pawfectplanner.ui.viewmodel

import androidx.lifecycle.*
import com.example.pawfectplanner.data.model.Task
import com.example.pawfectplanner.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(private val repo: TaskRepository) : ViewModel() {
    val allTasks: LiveData<List<Task>> = repo.allTasks.asLiveData()
    fun insert(t: Task) = viewModelScope.launch { repo.insert(t) }
    fun update(t: Task) = viewModelScope.launch { repo.update(t) }
    fun delete(t: Task) = viewModelScope.launch { repo.delete(t) }
}

class TaskViewModelFactory(private val repo: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(c: Class<T>) =
        if (c.isAssignableFrom(TaskViewModel::class.java))
            TaskViewModel(repo) as T
        else throw IllegalArgumentException("Unknown VM")
}
