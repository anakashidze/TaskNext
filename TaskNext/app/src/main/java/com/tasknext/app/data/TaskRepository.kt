package com.tasknext.app.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.concurrent.TimeUnit

class TaskRepository(private val dao: TaskDao, private val settings: SettingsStore) {

    fun observeAllTasks(): Flow<List<Task>> = dao.observeAllTasks()
    fun observeActiveSlots(): Flow<List<Task>> = dao.observeActiveSlots()
    fun observePool(): Flow<List<Task>> = dao.observePool()
    fun observeHistory(): Flow<List<CompletionLog>> = dao.observeHistory()

    private fun todayEpochDay(): Long =
        TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().timeInMillis)

    suspend fun addTask(task: Task): Long {
        val nextOrder = (dao.getAllOnce().maxOfOrNull { it.orderIndex } ?: -1) + 1
        val id = dao.insert(task.copy(orderIndex = nextOrder))
        // If there's no mandatory task assigned yet at all, this one becomes it.
        if (dao.getAllOnce().none { it.slot == 0 }) {
            dao.getById(id)?.let { dao.update(it.copy(slot = 0)) }
        }
        return id
    }

    suspend fun updateTask(task: Task) = dao.update(task)

    suspend fun deleteTask(id: Long) = dao.delete(id)

    suspend fun reorderPool(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id ->
            dao.getById(id)?.let { dao.update(it.copy(orderIndex = index)) }
        }
    }

    /** Pulls a bonus (non-mandatory) task from the pool into slot 1 or 2. */
    suspend fun activateBonusTask(taskId: Long) {
        val usedSlots = dao.getAllOnce().mapNotNull { it.slot }
        val freeSlot = listOf(1, 2).firstOrNull { it !in usedSlots } ?: return
        val task = dao.getById(taskId) ?: return
        dao.update(task.copy(slot = freeSlot))
    }

    /**
     * Call once when the app opens / resumes. If the calendar day has rolled
     * over since we last checked, clears the "day complete" flag and, if a
     * tomorrow-task was queued, promotes it into the mandatory slot (0).
     */
    suspend fun rolloverDayIfNeeded() {
        val today = todayEpochDay()
        if (settings.lastActiveEpochDay == today) return

        settings.dayCompletedEpochDay = -1L
        val queuedId = settings.queuedTomorrowTaskId
        if (queuedId != -1L) {
            dao.getById(queuedId)?.let { dao.update(it.copy(slot = 0)) }
            settings.queuedTomorrowTaskId = -1L
        }
        settings.lastActiveEpochDay = today
    }

    fun isDayComplete(): Boolean = settings.dayCompletedEpochDay == todayEpochDay()

    /** Completes the mandatory task: logs it, clears slot 0, marks the day done. */
    suspend fun completeMandatory(taskId: Long) {
        logCompletion(taskId)
        dao.getById(taskId)?.let { dao.update(it.copy(slot = null)) }
        settings.dayCompletedEpochDay = todayEpochDay()
    }

    /** Completes a bonus task: logs it, clears its slot. Does not affect day-complete status. */
    suspend fun completeBonus(taskId: Long) {
        logCompletion(taskId)
        dao.getById(taskId)?.let { dao.update(it.copy(slot = null)) }
    }

    private suspend fun logCompletion(taskId: Long) {
        val task = dao.getById(taskId) ?: return
        dao.insertLog(
            CompletionLog(
                taskId = task.id,
                titleSnapshot = task.title,
                colorHexSnapshot = task.colorHex,
                completedAtEpochMillis = Calendar.getInstance().timeInMillis
            )
        )
    }

    /** Queues a pool task to become tomorrow's mandatory task (does not activate it today). */
    suspend fun setTomorrowTask(taskId: Long) {
        settings.queuedTomorrowTaskId = taskId
    }

    fun queuedTomorrowTaskId(): Long = settings.queuedTomorrowTaskId

    /** Suggests a default for tomorrow: top of the pool queue. */
    suspend fun suggestNext(): Task? = dao.getAllOnce()
        .filter { it.slot == null }
        .minByOrNull { it.orderIndex }
}
