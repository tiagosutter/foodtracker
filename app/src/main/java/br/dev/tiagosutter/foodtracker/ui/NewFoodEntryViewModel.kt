package br.dev.tiagosutter.foodtracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.tiagosutter.foodtracker.database.FoodEntryDao
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewFoodEntryViewModel @Inject constructor(private val dao: FoodEntryDao) : ViewModel() {


    fun submitForm(foodEntry: FoodEntry) {
        viewModelScope.launch {
            dao.upsertFoodEntry(foodEntry)
        }
    }
}