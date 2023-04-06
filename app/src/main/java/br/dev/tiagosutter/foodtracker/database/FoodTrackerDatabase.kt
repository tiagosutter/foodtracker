package br.dev.tiagosutter.foodtracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import br.dev.tiagosutter.foodtracker.entities.FoodEntry

@Database(entities = [FoodEntry::class], version = 1, exportSchema = true)
abstract class FoodTrackerDatabase : RoomDatabase() {
    companion object {
        const val name = "FoodTrackerDatabase"
    }

    abstract fun foodEntryDao(): FoodEntryDao
}