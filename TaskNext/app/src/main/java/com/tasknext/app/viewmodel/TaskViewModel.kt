package com.tasknext.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tasknext.app.data.*
import com.tasknext.app.notifications.NotificationScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).taskDao()
    val settings = SettingsStore(application)
    private val repo = TaskRepository(dao, settings)

    // Slot 0 = the mandatory task for today (nullable if day is complete / none assigned yet).
    val mandatoryTask: StateFlow<Task?> = repo.observeActiveSlots()
        .map { slots -> slots.firstOrNull { it.slot == 0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Slots 1/2 = bonus / reminder-only tasks. Completing these never ends the day.
    val bonusTasks: StateFlow<List<Task>> = repo.observeActiveSlots()
        .map { slots -> slots.filter { it.slot != 0 }.sortedBy { it.slot } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pool: StateFlow<List<Task>> = repo.observePool()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<Task>> = repo.observeAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<CompletionLog>> = repo.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dayComplete = MutableStateFlow(false)
    val dayComplete: StateFlow<Boolean> = _dayComplete

    // Shown right after the mandatory task is completed, to pick tomorrow's focus.
    private val _showTomorrowPicker = MutableStateFlow(false)
    val showTomorrowPicker: StateFlow<Boolean> = _showTomorrowPicker

    init {
        viewModelScope.launch {
            repo.rolloverDayIfNeeded()
            _dayComplete.value = repo.isDayComplete()
        }
    }

    fun addTask(
        title: String, colorHex: String, priority: Priority,
        comment: String, reminderEnabled: Boolean, hour: Int?, minute: Int?
    ) = viewModelScope.launch {
        val task = Task(
            title = title, colorHex = colorHex, priority = priority, comment = comment,
            reminderEnabled = reminderEnabled, reminderHour = hour, reminderMinute = minute
        )
        val id = repo.addTask(task)
        if (reminderEnabled) {
            NotificationScheduler.schedule(
                getApplication(), task.copy(id = id),
                settings.defaultReminderHour, settings.defaultReminderMinute
            )
        }
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        repo.updateTask(task)
        NotificationScheduler.schedule(
            getApplication(), task, settings.defaultReminderHour, settings.defaultReminderMinute
        )
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        NotificationScheduler.cancel(getApplication(), task.id)
        repo.deleteTask(task.id)
    }

    fun reorderPool(orderedIds: List<Long>) = viewModelScope.launch {
        repo.reorderPool(orderedIds)
    }

    fun activateBonusFromPool(taskId: Long) = viewModelScope.launch {
        repo.activateBonusTask(taskId)
    }

    /** The one task that can end the day. Completing it locks the day as "done". */
    fun completeMandatory(taskId: Long) = viewModelScope.launch {
        repo.completeMandatory(taskId)
        _dayComplete.value = true
        _showTomorrowPicker.value = true
    }

    /** Bonus tasks: complete freely, never affects the day-complete state. */
    fun completeBonus(taskId: Long) = viewModelScope.launch {
        repo.completeBonus(taskId)
    }

    fun dismissTomorrowPicker() {
        _showTomorrowPicker.value = false
    }

    fun setTomorrowTask(taskId: Long) = viewModelScope.launch {
        repo.setTomorrowTask(taskId)
        _showTomorrowPicker.value = false
    }

    suspend fun suggestedTomorrow(): Task? = repo.suggestNext()

    fun queuedTomorrowTaskId(): Long = repo.queuedTomorrowTaskId()
}
