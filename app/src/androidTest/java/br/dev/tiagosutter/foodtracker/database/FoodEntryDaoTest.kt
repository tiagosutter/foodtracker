package br.dev.tiagosutter.foodtracker.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import br.dev.tiagosutter.foodtracker.entities.FoodEntryWithImages
import br.dev.tiagosutter.foodtracker.entities.SavedImage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@OptIn(ExperimentalCoroutinesApi::class)
@SmallTest
class FoodEntryDaoTest {

    companion object {
        private const val FAKE_INGREDIENTS = "Ingredients"
        private const val FAKE_SYMPTOMS = "Symptoms"
        private const val FAKE_DATE_AND_TIME = "2023-05-13T12:00"
    }

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    lateinit var dao: FoodEntryDao

    @Before
    fun setup() {
        val database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FoodTrackerDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.foodEntryDao()
    }

    @Test
    fun successfullyInsertsFoodEntry() = runTest {
        val toBeInserted = FoodEntry(FAKE_INGREDIENTS, FAKE_DATE_AND_TIME, FAKE_SYMPTOMS)

        val upsertId = dao.upsertFoodEntry(toBeInserted)

        val foodEntry = dao.getFoodEntryOrderedByDatetime().first().first()
        val expected = FoodEntry(FAKE_INGREDIENTS, FAKE_DATE_AND_TIME, FAKE_SYMPTOMS, upsertId)
        assertEquals(expected, foodEntry)
    }

    @Test
    fun successfullyDeletesInsertedFoodEntry() = runTest {
        val toBeInserted = FoodEntry(FAKE_INGREDIENTS, FAKE_DATE_AND_TIME, FAKE_SYMPTOMS)
        val upsertId = dao.upsertFoodEntry(toBeInserted)

        dao.deleteFoodEntry(toBeInserted.copy(foodEntryId = upsertId))

        val foodEntry = dao.getFoodEntryOrderedByDatetime().first()
        val expected = emptyList<FoodEntry>()
        assertEquals(expected, foodEntry)
    }

    @Test
    fun successfullyUpdatesInsertedFoodEntry() = runTest {
        val toBeInserted = FoodEntry(FAKE_INGREDIENTS, FAKE_DATE_AND_TIME, FAKE_SYMPTOMS)
        val upsertId = dao.upsertFoodEntry(toBeInserted)

        val expected = toBeInserted.copy(foodEntryId = upsertId, ingredients = "New Ingredients")
        dao.upsertFoodEntry(expected)

        val foodEntry = dao.getFoodEntryOrderedByDatetime().first().first()
        assertEquals(expected, foodEntry)
    }

    @Test
    fun successfullyInsertsEntryWithImages() = runTest {
        val toBeInserted = FoodEntryWithImages(
            FoodEntry(FAKE_INGREDIENTS, FAKE_DATE_AND_TIME, FAKE_SYMPTOMS),
            listOf(SavedImage("NAME", 0, 0))
        )
        val ids = dao.insertFoodEntryWithImages(toBeInserted)

        val insertedFoodEntryId = ids.first
        val foodEntryWithImages = dao.getFoodEntryWithImagesById(insertedFoodEntryId)
        val insertedSavedImageId = ids.second.first()
        val expected = FoodEntryWithImages(
            FoodEntry(FAKE_INGREDIENTS, FAKE_DATE_AND_TIME, FAKE_SYMPTOMS, insertedFoodEntryId),
            listOf(SavedImage("NAME", insertedFoodEntryId, insertedSavedImageId))
        )
        assertEquals(expected, foodEntryWithImages)
    }

}

