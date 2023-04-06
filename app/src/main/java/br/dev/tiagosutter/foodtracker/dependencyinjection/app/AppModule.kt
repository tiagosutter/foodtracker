package br.dev.tiagosutter.foodtracker.dependencyinjection.app

import android.content.Context
import androidx.room.Room
import br.dev.tiagosutter.foodtracker.FoodTrackerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @AppScope
    fun foodTrackerDatabase(@ApplicationContext applicationContext: Context): FoodTrackerDatabase =
        Room.databaseBuilder(
            applicationContext,
            FoodTrackerDatabase::class.java, FoodTrackerDatabase.name
        ).build()

    @Provides
    fun foodEntryDao(database: FoodTrackerDatabase) = database.foodEntryDao()
}