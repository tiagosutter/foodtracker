package br.dev.tiagosutter.foodtracker.ui.newentry

import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import br.dev.tiagosutter.foodtracker.R
import br.dev.tiagosutter.foodtracker.database.FoodEntryDao
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import br.dev.tiagosutter.foodtracker.ui.entrieslist.FoodEntriesFragment
import br.dev.tiagosutter.foodtracker.ui.entrieslist.FoodItemViewHolder
import br.dev.tiagosutter.foodtracker.util.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4ClassRunner::class)
class FoodEntriesFragmentTest {

    @get:Rule
    var hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var dao: FoodEntryDao

    @Before
    fun setup() = runTest {
        hiltRule.inject()
        dao.deleteAll()

        val testNavController = TestNavHostController(ApplicationProvider.getApplicationContext())
        launchFragmentInHiltContainer<FoodEntriesFragment> {
            Navigation.setViewNavController(requireView(), testNavController)
        }
    }


    @Test
    fun swipe_deletesItem() = runTest {
        // Arrange
        dao.upsertFoodEntry(FoodEntry("", "2023-05-13T12:00", ""))

        // Act
        onView(withId(R.id.foodEntriesRecyclerView)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1,
                swipeRight()
            )
        )

        // Assert
        assertTrue(dao.getFoodEntryOrderedByDatetime().first().isEmpty())
    }
}