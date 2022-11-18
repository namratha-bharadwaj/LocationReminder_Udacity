package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        stopKoin()
        dataSource = FakeDataSource()
        dataSource.reminders = createRemindersForDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    private fun createRemindersForDataSource(): MutableList<ReminderDTO> {
        val reminder1 = ReminderDTO(
            title = "Title1",
            description = "Description1",
            location = "location1",
            latitude = 0.0,
            longitude = 0.0
        )

        val reminder2 = ReminderDTO(
            title = "Title2",
            description = "Description2",
            location = "location2",
            latitude = 0.0,
            longitude = 0.0
        )
        return mutableListOf(reminder1, reminder2)
    }

    @Test
    fun loadReminders_addsRemindersToTheDb() = runBlockingTest {
        viewModel.loadReminders()
        assert(viewModel.remindersList.value?.isNotEmpty() == true)
        assert(viewModel.remindersList.value?.size == 2)
    }

    @Test
    fun givenEmptyDb_validateShowNoDataIsRendered() = runBlockingTest {
        dataSource.deleteAllReminders()

        viewModel.loadReminders()
        assert(viewModel.showNoData.value == true)
    }

    @Test
    fun givenRemindersInTheList_deleteReminders_showNoDataIsTrue() = runBlockingTest {
        //GIVEN: 2 reminders are insterted to DB in the setup

        //WHEN: delete reminders so that the DB is empty
        dataSource.deleteAllReminders()

        viewModel.loadReminders()
        assert(viewModel.remindersList.value?.size == 0)
        assert(viewModel.showNoData.value == true)
    }

    @Test
    fun givenRemindersInDb_whenErrorInLoadingReminders_thenShowsSnackBar() = runBlockingTest {
        //GIVEN: 2 reminders are inserted to DB in the setup
        // setReturnError in dataSource to true to return error for loading reminders
        dataSource.setReturnError(true)

        viewModel.loadReminders()
        var error = viewModel.showSnackBar.getOrAwaitValue()
        assert(error.contentEquals("GetRemindersMethod exception"))
    }

    @Test
    fun givenRemindersInDB_whenLoadRemindersIsCalled_thenSpinnerIsShown() {
        //GIVEN: 2 reminders are inserted to DB in the setup
        mainCoroutineRule.pauseDispatcher()
        viewModel.loadReminders()
        assert(viewModel.showLoading.getOrAwaitValue() == true)
        mainCoroutineRule.resumeDispatcher()
        assert(viewModel.showLoading.getOrAwaitValue() == false)
    }
}