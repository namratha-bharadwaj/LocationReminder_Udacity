package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false

    fun setReturnError(returnError: Boolean) {
        shouldReturnError = returnError
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        try {
            if (shouldReturnError) {
                return Result.Error("GetRemindersMethod exception")
            }
            reminders.let {
                return Result.Success(ArrayList(it))
            }
        } catch (e: Exception) {
            return Result.Error(e.message)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        try {
            if (shouldReturnError) {
                return Result.Error("Exception getReminder")
            }
            reminders.find {
                it.id == id
            }?.let {
                return Result.Success(it)
            } ?: return Result.Error("Exception reminder not found")
        } catch (e: Exception) {
            return Result.Error(e.message)
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }
}
