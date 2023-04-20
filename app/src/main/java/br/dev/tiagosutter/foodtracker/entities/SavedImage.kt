package br.dev.tiagosutter.foodtracker.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_image",
    foreignKeys = [ForeignKey(
        entity = FoodEntry::class,
        parentColumns = arrayOf("foodEntryId"),
        childColumns = arrayOf("savedImageId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class SavedImage(
    val name: String,
    val foodEntryId: Int,
    @PrimaryKey(autoGenerate = true)
    val savedImageId: Int = 0
) {
    fun isAlreadySaved(): Boolean = savedImageId != 0
}