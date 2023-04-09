package br.dev.tiagosutter.foodtracker.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_entry")
data class FoodEntry(
    // TODO: Look for solutions that do not involve using String for datetime, for type safety reasons.
    //  for now it will be alright
    val ingredients: String,
    val dateAndTime: String,
    val symptoms: String,
    @PrimaryKey(autoGenerate = true)
    val foodEntryId: Int = 0
)