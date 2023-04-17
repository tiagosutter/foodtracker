package br.dev.tiagosutter.foodtracker.ui.entrieslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.tiagosutter.foodtracker.database.FoodEntryDao
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class FoodEntriesViewModel @Inject constructor(private val dao: FoodEntryDao) : ViewModel() {

    data class FoodEntriesByDate(
        val date: LocalDate,
        val entries: List<FoodEntry>
    )

    private val _entries = MutableLiveData<List<FoodEntriesByDate>>()
    val entries: LiveData<List<FoodEntriesByDate>> = _entries

    fun getAllEntries() {
        viewModelScope.launch {
            dao.getFoodEntryOrderedByDatetime().collect { entries ->
                _entries.value = entries
                    .groupBy { it.getDateYearMonthDay() }
                    .map {
                        FoodEntriesByDate(LocalDate.parse(it.key), it.value)
                    }
            }
        }
    }

}