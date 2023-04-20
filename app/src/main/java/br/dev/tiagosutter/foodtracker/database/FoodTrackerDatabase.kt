package br.dev.tiagosutter.foodtracker.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import br.dev.tiagosutter.foodtracker.entities.SavedImage



@Database(
    version = 2,
    entities = [FoodEntry::class, SavedImage::class],
    exportSchema = true,
    autoMigrations = [
        AutoMigration (from = 1, to = 2)
    ])
abstract class FoodTrackerDatabase : RoomDatabase() {
    companion object {
        const val name = "FoodTrackerDatabase"
    }

    abstract fun foodEntryDao(): FoodEntryDao

    abstract fun savedImagesDao(): SavedImageDao
}