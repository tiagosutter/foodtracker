package br.dev.tiagosutter.foodtracker.dependencyinjection.app

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import br.dev.tiagosutter.foodtracker.BuildConfig
import br.dev.tiagosutter.foodtracker.database.FoodTrackerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @AppScope
    fun foodTrackerDatabase(@ApplicationContext applicationContext: Context): FoodTrackerDatabase {

        val databaseBuilder = Room.databaseBuilder(
            applicationContext,
            FoodTrackerDatabase::class.java, FoodTrackerDatabase.name
        )

        if (BuildConfig.DEBUG) {
            databaseBuilder.setQueryCallback(object : RoomDatabase.QueryCallback {
                override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
                    Log.d("FoodTrackerDatabase", "QueryCallback:: $sqlQuery")
                }
            }, Executors.newSingleThreadExecutor())
        }

        return databaseBuilder.build()
    }

    @Provides
    fun foodEntryDao(database: FoodTrackerDatabase) = database.foodEntryDao()
}