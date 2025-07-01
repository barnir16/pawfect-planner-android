package com.example.pawfectplanner.ui.viewmodel

import androidx.lifecycle.*
import com.example.pawfectplanner.data.model.Task
import com.example.pawfectplanner.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(private val repo: TaskRepository) : ViewModel() {
    val allTasks: LiveData<List<Task>> = repo.allTasks.asLiveData()
    fun insert(t: Task) = viewModelScope.launch { repo.insert(t) }
    fun update(t: Task) = viewModelScope.launch { repo.update(t) }
    fun delete(t: Task) = viewModelScope.launch { repo.delete(t) }
}


