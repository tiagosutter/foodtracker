package br.dev.tiagosutter.foodtracker.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.tiagosutter.foodtracker.database.FoodEntryDao
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class NewFoodEntryViewModel @Inject constructor(private val dao: FoodEntryDao) : ViewModel() {

    sealed class SaveResult {
        object IngredientsEmptyError : SaveResult()
        object DateTimeEmptyError : SaveResult()
        object Success : SaveResult()
    }

    private val _saveResult = MutableLiveData<SaveResult>()
    val saveResult: LiveData<SaveResult> = _saveResult

    fun submitForm(foodEntry: FoodEntry) {
        if (!validateForm(foodEntry)) {
            return
        }
        viewModelScope.launch {
            dao.upsertFoodEntry(foodEntry)
            _saveResult.value = SaveResult.Success
        }
    }

    private fun validateForm(foodEntry: FoodEntry): Boolean {
        var valid = true
        if (foodEntry.ingredients.isBlank()) {
            valid = false
            _saveResult.value = SaveResult.IngredientsEmptyError
        }
        try {
            LocalDateTime.parse(foodEntry.dateAndTime)
        } catch (e: DateTimeParseException) {
            valid = false
            _saveResult.value = SaveResult.DateTimeEmptyError
        }
        return valid
    }
}