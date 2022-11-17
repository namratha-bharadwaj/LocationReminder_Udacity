package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource
    private lateinit var remindersList: MutableList<ReminderDTO>

    @Before
    fun setup() {
        remindersList = mutableListOf()
        dataSource = FakeDataSource(remindersList)
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @Test
    fun givenAReminder_whenSaveReminderIsCalled_savesTheReminderToTheDB() = runBlockingTest {
        remindersList.clear()
        val reminder1 = ReminderDataItem(
            title = "Title1",
            description = "Description1",
            location = "location1",
            latitude = 0.0,
            longitude = 0.0
        )
        viewModel.saveReminder(reminder1)

        assert(remindersList.size == 1)
        assert(remindersList[0].title.equals("Title1"))

        val value = viewModel.showToast.value
        Assert.assertEquals(value, "Reminder Saved !")
    }

    @Test
    fun givenReminderDataIsNull_validateDataReturnsFalse() = runBlockingTest {
        val reminder: ReminderDataItem? = null

        val isEnteredDataValid = viewModel.validateEnteredData(reminder)
        assert(!isEnteredDataValid)
        val snackbarIntValue = viewModel.showSnackBarInt.value
        Assert.assertEquals(2131820601, snackbarIntValue)
    }

    @Test
    fun givenReminderLocationIsNull_validateDataReturnsFalse() = runBlockingTest {
        val reminder = ReminderDataItem(
            title = "Title1",
            description = "Description1",
            location = null,
            latitude = 0.0,
            longitude = 0.0
        )

        val isEnteredDataValid = viewModel.validateEnteredData(reminder)
        assert(!isEnteredDataValid)
        val snackbarIntValue = viewModel.showSnackBarInt.value
        Assert.assertEquals(2131820602, snackbarIntValue)
    }

    @Test
    fun givenNullTitle_validateDataReturnsFalse() = runBlockingTest {
        val reminderItem = ReminderDataItem(
            title = null,
            description = "description",
            location = "location",
            latitude = 0.0,
            longitude = 0.0
        )

        assert(!viewModel.validateEnteredData(reminderItem))
    }
}
