package br.dev.tiagosutter.foodtracker.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_image",
    foreignKeys = [ForeignKey(
        entity = FoodEntry::class,
        parentColumns = arrayOf("foodEntryId"),
        childColumns = arrayOf("parentFoodEntryId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class SavedImage(
    val name: String,
    @ColumnInfo(index = true)
    var parentFoodEntryId: Long,
    @PrimaryKey(autoGenerate = true)
    val savedImageId: Long = 0
)