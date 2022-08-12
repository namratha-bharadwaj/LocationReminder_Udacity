package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import java.util.*

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersDao: RemindersDao
    private lateinit var db: RemindersDatabase

    @Before
    fun setupDb() {
        val context = InstrumentationRegistry.getInstrumentation().context
        db = Room.inMemoryDatabaseBuilder(context, RemindersDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        remindersDao = db.reminderDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getReminder_returnsCorrectReminder() = runBlockingTest {
        //GIVEN: insert a reminder
        val id = UUID.randomUUID().toString()
        val reminder = ReminderDTO(
            title = "Sample title",
            description = "Sample description",
            location = "Sample location",
            latitude = 0.0,
            longitude = 0.0,
            id = id
        )

        remindersDao.saveReminder(reminder)

        // WHEN: Get reminder by ID
        val reminderFromDao = remindersDao.getReminderById(reminder.id)

        //THEN:
        assertThat(reminderFromDao, notNullValue())
        assertThat(reminderFromDao?.id, `is`(reminder.id))
        assertThat(reminderFromDao?.title, `is`(reminder.title))
        assertThat(reminderFromDao?.description, `is`(reminder.description))
        assertThat(reminderFromDao?.location, `is`(reminder.location))
        assertThat(reminderFromDao?.latitude, `is`(reminder.latitude))
        assertThat(reminderFromDao?.longitude, `is`(reminder.longitude))

    }

    @Test
    fun getReminders_returnAllTheRemindersInDb() = runBlockingTest {
        // GIVEN - Insert 2 reminders to the DB
        val reminder1 = ReminderDTO(
            title = "Sample title1",
            description = "Sample description1",
            location = "Sample location1",
            latitude = 0.0,
            longitude = 0.0,
        )

        val reminder2 = ReminderDTO(
            title = "Sample title2",
            description = "Sample description2",
            location = "Sample location2",
            latitude = 0.0,
            longitude = 0.0
        )

        remindersDao.saveReminder(reminder1)
        remindersDao.saveReminder(reminder2)

        //WHEN: get reminders from DB
        val remindersList = remindersDao.getReminders()

        //THEN: assert 2 reminders are returned
        assertThat(remindersList.size, `is`(2))
        assertThat(remindersList.first().title, `is`("Sample title1"))
        assertThat(remindersList.first().description, `is`("Sample description1"))
        assertThat(remindersList.first().location, `is`("Sample location1"))
    }

    @Test
    fun deleteAllReminders_clearsTheDb() = runBlockingTest {
        // GIVEN - Insert 2 reminders to the DB
        val reminder1 = ReminderDTO(
            title = "Sample title1",
            description = "Sample description1",
            location = "Sample location1",
            latitude = 0.0,
            longitude = 0.0,
        )

        val reminder2 = ReminderDTO(
            title = "Sample title2",
            description = "Sample description2",
            location = "Sample location2",
            latitude = 0.0,
            longitude = 0.0
        )

        remindersDao.saveReminder(reminder1)
        remindersDao.saveReminder(reminder2)

        val getReminders = remindersDao.getReminders()
        assertThat(getReminders.size, `is`(2))

        //WHEN: delete all reminders
        remindersDao.deleteAllReminders()

        //THEN: result of getReminders() list should be 0
        val remindersListAfterDeletion = remindersDao.getReminders()
        assertThat(remindersListAfterDeletion.size, `is`(0))

    }
}