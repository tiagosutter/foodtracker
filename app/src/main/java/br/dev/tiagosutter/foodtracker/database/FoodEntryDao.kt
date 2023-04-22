package br.dev.tiagosutter.foodtracker.database

import android.util.Log
import androidx.room.*
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import br.dev.tiagosutter.foodtracker.entities.FoodEntryWithImages
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.util.*

@Dao
abstract class FoodEntryDao(database: FoodTrackerDatabase) {

    private var savedImagesDao: SavedImageDao

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
        val upsertedFoodEntryId = upsertFoodEntry(foodEntyWithImages.foodEntry)
        if (upsertedFoodEntryId != -1L) {
            for (image in foodEntyWithImages.images) {
                image.parentFoodEntryId = upsertedFoodEntryId
            }
        }
        savedImagesDao.insertSavedImages(foodEntyWithImages.images)
    }

    @Transaction
    @Query("SELECT * FROM food_entry WHERE foodEntryId = :id")
    abstract fun getFoodEntryWithImagesById(id: Long): Flow<FoodEntryWithImages>
}