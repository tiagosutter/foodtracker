package br.dev.tiagosutter.foodtracker.ui.newentry

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import br.dev.tiagosutter.foodtracker.common.MainDispatcherRule
import br.dev.tiagosutter.foodtracker.common.getOrAwaitValue
import br.dev.tiagosutter.foodtracker.database.FoodEntryDao
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import br.dev.tiagosutter.foodtracker.entities.FoodEntryWithImages
import br.dev.tiagosutter.foodtracker.entities.SavedImage
import br.dev.tiagosutter.foodtracker.ui.entrieslist.FoodEntriesScreenViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import org.mockito.kotlin.*
import org.mockito.kotlin.any as anything


@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class NewFoodEntryViewModelTest {

    // region constants
    companion object {

    }
    // endregion constants


    // region helper fields
    @Mock
    lateinit var foodEntryDao: FoodEntryDao

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    var mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()
    // endregion helper fields



    lateinit var viewModel: NewFoodEntryViewModel

    @Before
    @Throws(Exception::class)
    fun setup() {
        viewModel = NewFoodEntryViewModel(foodEntryDao)
    }

    @Test
    fun submitForm_emptyDateTime_resultsInDateTimeEmptyError() {
        // Arrange
        val foodEntry = FoodEntry("Some ingredients", "", "")
        // Act
        viewModel.submitForm(foodEntry)

        // Assert
        val saveResult = viewModel.saveResult.getOrAwaitValue()
        val expectedResult = NewFoodEntryViewModel.SaveResult.DateTimeEmptyError
        assertEquals(saveResult, expectedResult)
    }

    @Test
    fun submitForm_emptyTime_resultsInDateTimeEmptyError() {
        // Arrange
        val foodEntry = FoodEntry("Some ingredients", "2023-01-01", "")
        // Act
        viewModel.submitForm(foodEntry)

        // Assert
        val saveResult = viewModel.saveResult.getOrAwaitValue()
        val expectedResult = NewFoodEntryViewModel.SaveResult.DateTimeEmptyError
        assertEquals(saveResult, expectedResult)
    }

    @Test
    fun submitForm_emptyIngredientsAndDateTime_resultsInValidationErrors() {
        // Arrange
        val mockObserver: Observer<NewFoodEntryViewModel.SaveResult> = mock()
        val foodEntry = FoodEntry("", "", "")
        viewModel.saveResult.observeForever(mockObserver)

        // Act
        viewModel.submitForm(foodEntry)

        // Assert
        mockObserver.inOrder {
            verify(mockObserver).onChanged(NewFoodEntryViewModel.SaveResult.IngredientsEmptyError)
            verify(mockObserver).onChanged(NewFoodEntryViewModel.SaveResult.DateTimeEmptyError)
        }
    }

    @Test
    fun submitForm_validFoodEntry_resultsInSuccess() {
        // Arrange
        val mockObserver: Observer<NewFoodEntryViewModel.SaveResult> = mock()
        val foodEntry = FoodEntry("Some ingredients", "2023-05-01T12:00", "")
        viewModel.saveResult.observeForever(mockObserver)

        // Act
        viewModel.submitForm(foodEntry)

        // Assert
        verify(mockObserver, times(1)).onChanged(NewFoodEntryViewModel.SaveResult.Success)
    }

    @Test
    fun submitForm_validFoodEntry_foodEntrySentToDao() = runTest {
        // Arrange
        val foodEntry = FoodEntry("Some ingredients", "2023-05-01T12:00", "")
        val foodEntryWithImages = FoodEntryWithImages(foodEntry, listOf())

        // Act
        viewModel.submitForm(foodEntry)

        // Assert
        verify(foodEntryDao).insertFoodEntryWithImages(foodEntryWithImages)
    }

    @Test
    fun submitForm_validFoodEntryWithImages_foodEntrySentToDaoWithImages() = runTest {
        // Arrange
        val foodEntry = FoodEntry("Some ingredients", "2023-05-01T12:00", "")
        val takenPicture = NewFoodEntryViewModel.TakenPicture(mock(), "NAME")
        viewModel.pictureTaken(takenPicture)
        val savedImage = SavedImage("NAME", 0)
        val foodEntryWithImages = FoodEntryWithImages(foodEntry, listOf(savedImage))

        // Act
        viewModel.submitForm(foodEntry)

        // Assert
        verify(foodEntryDao).insertFoodEntryWithImages(foodEntryWithImages)
    }

    // pictureTaken quando novai imagem registrada lista de imagens tem nova imagem
    @Test
    fun pictureTaken_imageAddedToAllImages() = runTest {
        // Arrange
        val takenPicture = NewFoodEntryViewModel.TakenPicture(mock(), "NAME")

        // Act
        viewModel.pictureTaken(takenPicture)
        val allImages = viewModel.allImages.getOrAwaitValue()

        // Assert
        allImages.contains(SavedImage("NAME", 0))
    }

    // region helper methods

    // endregion helper methods


    // region helper classes

    // endregion helper classes
}