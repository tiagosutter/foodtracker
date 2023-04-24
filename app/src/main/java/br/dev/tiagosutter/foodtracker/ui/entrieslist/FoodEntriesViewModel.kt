package br.dev.tiagosutter.foodtracker.ui.entrieslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.tiagosutter.foodtracker.database.FoodEntryDao
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import br.dev.tiagosutter.foodtracker.entities.FoodEntryWithImages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class FoodEntriesViewModel @Inject constructor(private val dao: FoodEntryDao) : ViewModel() {

    private data class FoodEntriesByDate(
        val date: LocalDate,
        val entries: List<FoodEntry>
    )

    private val _viewState = MutableLiveData<FoodEntriesScreenViewState>()
    val viewState: LiveData<FoodEntriesScreenViewState> = _viewState

    fun getAllEntries() {
        viewModelScope.launch {
            dao.getFoodEntryOrderedByDatetime().collect { entries ->
                val foodEntriesByDate: List<FoodEntriesByDate> = mapToEntriesByDate(entries)

                val foodEntryItems = foodEntriesByDate.toFoodEntryListItem()
                _viewState.value = FoodEntriesScreenViewState(foodEntryItems)
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

    private var mostRecentDeletion: FoodEntryWithImages? = null

    fun deleteEntry(foodEntry: FoodEntry) {
        viewModelScope.launch {
            registerAndDelete(foodEntry)
        }
    }

    private suspend fun registerAndDelete(foodEntry: FoodEntry) {
        val foodEntryWithImages = dao.getFoodEntryWithImagesById(foodEntry.foodEntryId)
        mostRecentDeletion = foodEntryWithImages
        dao.deleteFoodEntry(foodEntry)
    }

    fun undoLatestDeletion() {
        viewModelScope.launch {
            mostRecentDeletion?.let { nonNullFoodEntryWithImages ->
                dao.insertFoodEntryWithImages(nonNullFoodEntryWithImages)
            }
        }
    }
}