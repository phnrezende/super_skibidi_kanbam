
package com.pedro.task.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pedro.task.data.model.Task

class TaskViewModel : ViewModel() {

    private val _taskList = MutableLiveData<List<Task>>(emptyList())
    val taskList: LiveData<List<Task>> = _taskList

    private val _taskUpdate = MutableLiveData<Task>()
    val taskUpdate: LiveData<Task> = _taskUpdate

    fun setAllTasks(list: List<Task>) {
        _taskList.value = list
    }

    fun setUpdateTask(task: Task) {
        _taskUpdate.value = task
    }
}