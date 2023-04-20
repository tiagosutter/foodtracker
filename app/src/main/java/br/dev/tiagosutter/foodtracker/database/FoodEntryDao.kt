package br.dev.tiagosutter.foodtracker.database

import androidx.room.*
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import br.dev.tiagosutter.foodtracker.entities.FoodEntryWithImages
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FoodEntryDao(database: FoodTrackerDatabase) {

    private lateinit var savedImagesDao: SavedImageDao

    init {
        savedImagesDao = database.savedImagesDao()
    }

    @Upsert
    abstract suspend fun upsertFoodEntry(foodEntry: FoodEntry): Long

    @Delete
    abstract suspend fun deleteFoodEntry(foodEntry: FoodEntry)

    @Query("SELECT * FROM food_entry ORDER BY dateAndTime")
    abstract fun getFoodEntryOrderedByDatetime(): Flow<List<FoodEntry>>

    @Transaction
    open suspend fun insertFoodEntryWithImages(foodEntyWithImages: FoodEntryWithImages) {
        savedImagesDao.insertSavedImages(foodEntyWithImages.images)
        upsertFoodEntry(foodEntyWithImages.foodEntry)
    }

    @Transaction
    @Query("SELECT * FROM food_entry WHERE foodEntryId = :id ORDER BY dateAndTime")
    abstract fun getFoodEntryWithImagesById(id: Int): Flow<FoodEntryWithImages>
}