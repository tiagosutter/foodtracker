package br.dev.tiagosutter.foodtracker.entities

import androidx.room.Embedded
import androidx.room.Relation

data class FoodEntryWithImages(
    @Embedded
    val foodEntry: FoodEntry,
    @Relation(
        parentColumn = "foodEntryId",
        entityColumn = "savedImageId"
    )
    val images: List<SavedImage>
)
