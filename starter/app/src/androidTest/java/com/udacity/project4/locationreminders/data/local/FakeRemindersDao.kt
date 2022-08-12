package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import java.lang.Exception

class FakeRemindersDao: RemindersDao {

    private var returnError = false

    private var remindersServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    fun shouldReturnError(showError: Boolean) {
        returnError = showError

    }

    override suspend fun getReminders(): List<ReminderDTO> {
        if (returnError) {
            throw (Exception("Exception: at fun getReminders in the FakeRemindersDao class"))
            return emptyList()
        }

        var remindersList = mutableListOf<ReminderDTO>()
        remindersList.addAll(remindersServiceData.values)
        return remindersList
    }

    override suspend fun getReminderById(reminderId: String): ReminderDTO? {
        if (returnError) {
            throw (Exception("Exception: at fun getReminderById in the FakeRemindersDao class"))
            return null
        }

        remindersServiceData[reminderId]?.let {
            return it
        }
        return null
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersServiceData[reminder.id] = reminder
    }

    override suspend fun deleteAllReminders() {
        remindersServiceData.clear()
    }
}