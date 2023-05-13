package br.dev.tiagosutter.foodtracker.ui.newentry

import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import br.dev.tiagosutter.foodtracker.R
import br.dev.tiagosutter.foodtracker.database.FoodEntryDao
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import br.dev.tiagosutter.foodtracker.util.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.Matchers.not
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4ClassRunner::class)
class NewFoodEntryFragmentTest {

    @get:Rule
    var hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
        val testNavController = TestNavHostController(ApplicationProvider.getApplicationContext())

        val newFoodEntryFragmentArgs: NewFoodEntryFragmentArgs =
            NewFoodEntryFragmentArgs.Builder(null, "").build()
        launchFragmentInHiltContainer<NewFoodEntryFragment>(
            fragmentArgs = newFoodEntryFragmentArgs.toBundle(),
        ) {
//            testNavController.setGraph(R.id.nav_graph)
            Navigation.setViewNavController(requireView(), testNavController)
        }

    }

    @Test
    fun save_invalidForm_errorsShown() {
        onView(withId(R.id.saveButton)).perform(click())
        onView(withId(R.id.dateTimeRequiredErroTextView)).check(
            matches(allOf(isDisplayed(), withText(R.string.moment_of_the_day_is_required)))
        )
        onView(withId(R.id.ingredientsTextInputLayout))
            .check(matches(hasDescendant(withText(R.string.ingredients_required))))
    }

    @Test
    fun save_validFormData_resultsInNoErros() {
        onView(withId(R.id.dateInput)).perform(click())
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.timeOfDayInput)).perform(click())
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.ingredientsEditText)).perform(typeText("Some ingredients"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.saveButton)).perform(scrollTo(), click())

        onView(withId(R.id.dateTimeRequiredErroTextView)).check(
            matches(not(isDisplayed()))
        )
        onView(withId(R.id.ingredientsTextInputLayout))
            .check(
                matches(
                    not(hasDescendant(withText(R.string.ingredients_required)))
                )
            )
    }

    private fun launchWithEntry() {
        val foodEntry = FoodEntry("Ingredients", "2023-04-27T12:00", "")
        val newFoodEntryFragmentArgs: NewFoodEntryFragmentArgs =
            NewFoodEntryFragmentArgs.Builder(null, "").build()
        val scenario = launchFragmentInHiltContainer<NewFoodEntryFragment>(
            fragmentArgs = newFoodEntryFragmentArgs.toBundle(),
        )
    }
}