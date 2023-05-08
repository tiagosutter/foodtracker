package br.dev.tiagosutter.foodtracker.ui.entrieslist

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.tiagosutter.foodtracker.database.FoodEntryDao
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import br.dev.tiagosutter.foodtracker.entities.FoodEntryWithImages
import br.dev.tiagosutter.foodtracker.notifications.NotificationScheduler
import br.dev.tiagosutter.foodtracker.settings.PreferenceStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class FoodEntriesViewModel @Inject constructor(
    private val dao: FoodEntryDao,
    private val notificationScheduler: NotificationScheduler,
    private val preferences: PreferenceStore
) : ViewModel() {

    private data class FoodEntriesByDate(
        val date: LocalDate,
        val entries: List<FoodEntry>
    )
    private var mostRecentDeletion: FoodEntryWithImages? = null

    private val _viewState = MutableLiveData<FoodEntriesScreenViewState>()
    val viewState: LiveData<FoodEntriesScreenViewState> = _viewState

    private val _scheduleNotificationResult = MutableLiveData<NotificationScheduler.SchedulingResult>()
    val scheduleNotificationResult: LiveData<NotificationScheduler.SchedulingResult> =
        _scheduleNotificationResult

    init {
        scheduleNotificationsIfEnabled()
    }

    private fun scheduleNotificationsIfEnabled() {
        val notificationEnabled = preferences.getDailyNotificationsEnabled()

        if (notificationEnabled) {
            val schedulingResult = notificationScheduler.schedule()
            _scheduleNotificationResult.value = schedulingResult
        }
    }

    fun handleNotificationPermission(isGranted: Boolean) {
        if (isGranted) {
            scheduleNotificationsIfEnabled()
        } else {
            preferences.disabledNotifications()
        }
    }

    fun getAllEntries() {
        viewModelScope.launch {
            dao.getFoodEntryOrderedByDatetime().collect { entries ->
                val foodEntriesByDate: List<FoodEntriesByDate> = mapToEntriesByDate(entries)

                val foodEntryItems = foodEntriesByDate.toFoodEntryListItem()
                _viewState.value = FoodEntriesScreenViewState(foodEntryItems)
            }
        }
    }

    fun deleteEntry(foodEntry: FoodEntry) {
        viewModelScope.launch {
            registerAndDelete(foodEntry)
        }
    }

    fun undoLatestDeletion() {
        viewModelScope.launch {
            mostRecentDeletion?.let { nonNullFoodEntryWithImages ->
                dao.insertFoodEntryWithImages(nonNullFoodEntryWithImages)
            }
        }
    }

    private fun List<FoodEntriesByDate>.toFoodEntryListItem(): MutableList<FoodEntryListItemsViewState> {
        val list: MutableList<FoodEntryListItemsViewState> = mutableListOf()
        val foodEntriesByDate = this
        for (entry in foodEntriesByDate) {
            val dateTimeSeparator = FoodEntryListItemsViewState.DateEntry(entry.date)
            val foodEntries = entry.entries.map {
                FoodEntryListItemsViewState.FoodItem(it)
            }
            list.add(dateTimeSeparator)
            list.addAll(foodEntries)
        }
        return list
    }

    private fun mapToEntriesByDate(entries: List<FoodEntry>) =
        entries.groupBy { it.getDateYearMonthDay() }
            .map { FoodEntriesByDate(LocalDate.parse(it.key), it.value) }

    private suspend fun registerAndDelete(foodEntry: FoodEntry) {
        val foodEntryWithImages = dao.getFoodEntryWithImagesById(foodEntry.foodEntryId)
        mostRecentDeletion = foodEntryWithImages
        dao.deleteFoodEntry(foodEntry)
    }




}