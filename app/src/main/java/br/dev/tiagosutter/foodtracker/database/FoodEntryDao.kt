package br.dev.tiagosutter.foodtracker.database

import androidx.room.*
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import br.dev.tiagosutter.foodtracker.entities.FoodEntryWithImages
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
abstract class FoodEntryDao(database: FoodTrackerDatabase) {

    private var savedImagesDao: SavedImageDao = database.savedImagesDao()

    @Upsert
    abstract suspend fun upsertFoodEntry(foodEntry: FoodEntry): Long

    @Delete
    abstract suspend fun deleteFoodEntry(foodEntry: FoodEntry)

    @Query("SELECT * FROM food_entry ORDER BY dateAndTime")
    abstract fun getFoodEntryOrderedByDatetime(): Flow<List<FoodEntry>>

    @Transaction
    open suspend fun insertFoodEntryWithImages(foodEntryWithImages: FoodEntryWithImages): Pair<Long, List<Long>> {
        val upsertedFoodEntryId = upsertFoodEntry(foodEntryWithImages.foodEntry)
        if (upsertedFoodEntryId != -1L) {
            for (image in foodEntryWithImages.images) {
                image.parentFoodEntryId = upsertedFoodEntryId
            }
        }
        val savedImagesIds = savedImagesDao.insertSavedImages(foodEntryWithImages.images)
        return Pair(upsertedFoodEntryId, savedImagesIds)
    }

    @Transaction
    @Query("SELECT * FROM food_entry WHERE foodEntryId = :id")
    abstract suspend fun getFoodEntryWithImagesById(id: Long): FoodEntryWithImages

    @Query("DELETE FROM food_entry")
    abstract suspend fun deleteAll()
}