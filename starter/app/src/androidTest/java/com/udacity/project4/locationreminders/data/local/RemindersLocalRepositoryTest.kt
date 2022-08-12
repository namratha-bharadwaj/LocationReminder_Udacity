package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.utils.CommonConstants
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule=InstantTaskExecutorRule()

    private lateinit var remindersDao: FakeRemindersDao
    private lateinit var remindersRepository: RemindersLocalRepository

    @Before
    fun setup() {
        remindersDao = FakeRemindersDao()
        remindersRepository = RemindersLocalRepository(remindersDao, Dispatchers.Unconfined)
    }

    @Test
    fun getReminders_returnsAllTheReminders() = runBlockingTest {
        //GIVEN: create 2 reminders and save them to the DB
        val reminder1 = ReminderDTO(
            title = "Title1",
            description = "description1",
            location = "location1",
            latitude = 0.0,
            longitude = 0.0
        )

        val reminder2 = ReminderDTO(
            title = "Title2",
            description = "description2",
            location = "location2",
            latitude = 0.0,
            longitude = 0.0
        )

        remindersDao.saveReminder(reminder1)
        remindersDao.saveReminder(reminder2)

        //WHEN: call getReminders()
        val getRemindersResult = remindersRepository.getReminders() as Result.Success

        //THEN: returns all the reminders in DB
        assertThat(getRemindersResult.data, `is`(notNullValue()))
        assertThat(getRemindersResult.data.size, `is`(2))
        assertThat(getRemindersResult.data.first().title, `is`("Title1"))
        assertThat(getRemindersResult.data.last().title, `is`("Title2"))
    }

    @Test
    fun getRemindersById_returnsTheReminderThatMatchesTheId() = runBlockingTest {
        //GIVEN: create 2 reminders and save them to the DB
        val id1 = UUID.randomUUID().toString()
        val id2 = UUID.randomUUID().toString()
        val reminder1 = ReminderDTO(
            title = "Title1",
            description = "description1",
            location = "location1",
            latitude = 0.0,
            longitude = 0.0,
            id = id1
        )

        val reminder2 = ReminderDTO(
            title = "Title2",
            description = "description2",
            location = "location2",
            latitude = 0.0,
            longitude = 0.0,
            id = id2
        )

        remindersDao.saveReminder(reminder1)
        remindersDao.saveReminder(reminder2)

        //WHEN: call getReminderByID() with the ID of reminder1
        val getReminderbyID = remindersRepository.getReminder(id1) as Result.Success

        //THEN: variable getReminderbyID should be nonNull, and the values should match reminder1
        assertThat(getReminderbyID, `is`(notNullValue()))
        assertThat(getReminderbyID.data.title, `is`(reminder1.title))
        assertThat(getReminderbyID.data.description, `is`(reminder1.description))
        assertThat(getReminderbyID.data.location, `is`(reminder1.location))
    }

    @Test
    fun ifReminderNotPresentInDb_getReminderByID_returnsReminderNotFoundError() = runBlockingTest {

        remindersRepository.deleteAllReminders()
        val reminder = remindersRepository.getReminder(UUID.randomUUID().toString()) as Result.Error

        assertThat(reminder.message, `is`(CommonConstants.REMINDER_NOT_FOUND_ERROR))

    }

    @Test
    fun deleteReminders_clearsReminersInTheDB() = runBlockingTest {
        //GIVEN: create 2 reminders and save them to the DB
        val id1 = UUID.randomUUID().toString()
        val id2 = UUID.randomUUID().toString()
        val reminder1 = ReminderDTO(
            title = "Title1",
            description = "description1",
            location = "location1",
            latitude = 0.0,
            longitude = 0.0,
            id = id1
        )

        val reminder2 = ReminderDTO(
            title = "Title2",
            description = "description2",
            location = "location2",
            latitude = 0.0,
            longitude = 0.0,
            id = id2
        )

        remindersDao.saveReminder(reminder1)
        remindersDao.saveReminder(reminder2)

        val getRemindersResult = remindersRepository.getReminders() as Result.Success
        assertThat(getRemindersResult, `is`(notNullValue()))
        assertThat(getRemindersResult.data.size, `is`(2))

        //WHEN: delete reminders from the db
        remindersRepository.deleteAllReminders()

        val getRemindersResultAfterDeletion = remindersRepository.getReminders() as Result.Success

        assertThat(getRemindersResultAfterDeletion, `is`(notNullValue()))
        assertThat(getRemindersResultAfterDeletion.data.size, `is`(0))
    }

}