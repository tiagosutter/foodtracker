package br.dev.tiagosutter.foodtracker.ui.entrieslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import br.dev.tiagosutter.foodtracker.common.MainDispatcherRule
import br.dev.tiagosutter.foodtracker.common.getOrAwaitValue
import br.dev.tiagosutter.foodtracker.database.FoodEntryDao
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import br.dev.tiagosutter.foodtracker.entities.FoodEntryWithImages
import br.dev.tiagosutter.foodtracker.entities.SavedImage
import br.dev.tiagosutter.foodtracker.notifications.NotificationScheduler
import br.dev.tiagosutter.foodtracker.settings.PreferenceStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class FoodEntriesViewModelTest {

    // region constants
    companion object {
        private val FAKE_SAVED_IMAGES = listOf(SavedImage("name", 1, 1))
        private val FAKE_FOOD_ENTRIES = listOf(
            FoodEntry("Ingredient 1", "2023-04-25", "", 1),
            FoodEntry("Ingredient 2", "2023-04-25", "", 2),
            FoodEntry("Ingredient 3", "2023-04-26", "", 3),
        )
        private val dateSeparator1 = LocalDate.parse(FAKE_FOOD_ENTRIES[0].getDateYearMonthDay())
        private val dateSeparator2 = LocalDate.parse(FAKE_FOOD_ENTRIES[2].getDateYearMonthDay())
    }
    // endregion constants

    // region helper fields
    @Mock
    lateinit var foodEntryDao: FoodEntryDao

    @Mock
    lateinit var notificationScheduler: NotificationScheduler

    @Mock
    lateinit var preferenceStore: PreferenceStore

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    var mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()


    private val dispatcher = StandardTestDispatcher()
    // endregion helper fields

    private lateinit var viewModel: FoodEntriesViewModel

    @Before
    @Throws(Exception::class)
    fun setup() {
        viewModel = FoodEntriesViewModel(foodEntryDao, notificationScheduler, preferenceStore)
    }

    @Test
    fun getAllEntries_whenNoEntries_observerNotCalled() {
        // Arrange
        val mockObserver: Observer<FoodEntriesScreenViewState> = mock()
        setupNoEntries()

        // Act
        viewModel.getAllEntries()
        viewModel.viewState.observeForever(mockObserver)

        // Assert
        verifyNoInteractions(mockObserver)
    }

    @Test
    fun getAllEntries_entriesExist_viewStateGetsExpectedEntries() {
        // Arrange
        setupToHaveEntires()

        // Act
        viewModel.getAllEntries()

        // Assert
        val result = viewModel.viewState.getOrAwaitValue()
        val expectedList = listOf(
            FoodEntryListItemsViewState.DateEntry(dateSeparator1),
            FoodEntryListItemsViewState.FoodItem(FAKE_FOOD_ENTRIES[0]),
            FoodEntryListItemsViewState.FoodItem(FAKE_FOOD_ENTRIES[1]),
            FoodEntryListItemsViewState.DateEntry(dateSeparator2),
            FoodEntryListItemsViewState.FoodItem(FAKE_FOOD_ENTRIES[2]),
        )
        val expectedResult = FoodEntriesScreenViewState(
            expectedList
        )

        assertThat(result, equalTo(expectedResult))
    }

    @Test
    fun deleteEntry_callsDaoForDeletion() = runTest {
        // Arrange
        val foodEntryToDelete = FAKE_FOOD_ENTRIES[0]

        // Act
        viewModel.deleteEntry(foodEntryToDelete)

        // Assert
        verify(foodEntryDao).deleteFoodEntry(FAKE_FOOD_ENTRIES[0])
    }

    @Test
    fun deleteEntry_insertsBackExpectedData() = runTest {
        // Arrange
        val foodEntryToDelete = FAKE_FOOD_ENTRIES[0]
        setupToHaveImages(foodEntryToDelete)
        viewModel.deleteEntry(foodEntryToDelete)

        // Act
        viewModel.undoLatestDeletion()

        // Assert
        val expectedInsertion = FoodEntryWithImages(foodEntryToDelete, FAKE_SAVED_IMAGES)
        verify(foodEntryDao).insertFoodEntryWithImages(expectedInsertion)
    }

    // region helper methods
    private fun setupToHaveEntires() {
        foodEntryDao.stub {
            on { foodEntryDao.getFoodEntryOrderedByDatetime() }
                .doReturn(flowOf(FAKE_FOOD_ENTRIES))
        }
    }

    private fun setupNoEntries() {
        foodEntryDao.stub {
            on { foodEntryDao.getFoodEntryOrderedByDatetime() }
                .doReturn(flowOf())
        }
    }

    private fun setupToHaveImages(foodEntryToDelete: FoodEntry) {
        foodEntryDao.stub {
            onBlocking { getFoodEntryWithImagesById(1) }
                .doReturn(FoodEntryWithImages(foodEntryToDelete, FAKE_SAVED_IMAGES))
        }
    }
    // endregion helper methods

}
