package br.dev.tiagosutter.foodtracker.database

import androidx.room.*
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {

    @Upsert
    suspend fun upsertFoodEntry(foodEntry: FoodEntry): Long

    @Delete
    suspend fun deleteFoodEntry(foodEntry: FoodEntry)

    @Query("SELECT * FROM food_entry ORDER BY dateAndTime")
    fun getFoodEntryOrderedByDatetime(): Flow<List<FoodEntry>>
}