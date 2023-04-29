package br.dev.tiagosutter.foodtracker.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

private const val dateEndIndex = 10

@Entity(tableName = "food_entry")
data class FoodEntry(
    // TODO: Look for solutions that do not involve using String for datetime, for type safety reasons.
    //  for now it will be alright
    val ingredients: String,
    val dateAndTime: String,
    val symptoms: String,
    @PrimaryKey(autoGenerate = true)
    val foodEntryId: Long = 0L
) : Serializable {
    /**
     * Tries to get date yyyy-mm-dd by getting the dateAndTime substring
     */
    fun getDateYearMonthDay() = when {
        dateAndTime.length >= dateEndIndex -> dateAndTime.substring(0, dateEndIndex)
        else -> ""
    }

    /**
     * Tries to get time of day by getting the dateAndTime substring
     */
    fun getTimeOfDay() = when {
        dateAndTime.length >= dateEndIndex + 1 -> dateAndTime.substring(dateEndIndex + 1)
        else -> ""
    }


}