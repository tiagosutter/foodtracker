package br.dev.tiagosutter.foodtracker.ui.entrieslist

import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import java.time.LocalDate

sealed class FoodEntryListItemsViewState {
    data class DateEntry(val date: LocalDate) : FoodEntryListItemsViewState()
    data class FoodItem(val foodEntry: FoodEntry) : FoodEntryListItemsViewState()

    val id: String
        get() {
            return when (this) {
                is DateEntry -> this.date.toString()
                is FoodItem -> this.foodEntry.foodEntryId.toString()
            }
        }
}

data class FoodEntriesScreenViewState(
    val entriesByDate: List<FoodEntryListItemsViewState>
)