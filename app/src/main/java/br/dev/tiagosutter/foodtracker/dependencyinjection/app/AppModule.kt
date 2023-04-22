package br.dev.tiagosutter.foodtracker.dependencyinjection.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import androidx.room.RoomDatabase
import br.dev.tiagosutter.foodtracker.BuildConfig
import br.dev.tiagosutter.foodtracker.NotificationScheduler
import br.dev.tiagosutter.foodtracker.database.FoodTrackerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import java.util.concurrent.Executors

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun alarmManager(@ApplicationContext applicationContext: Context): AlarmManager =
        applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides
    fun notificationManager(@ApplicationContext applicationContext: Context): NotificationManagerCompat =
        NotificationManagerCompat.from(applicationContext)

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
                    Timber.tag("FoodTrackerDatabase").d("QueryCallback:: %s", sqlQuery)
                    Timber.tag("FoodTrackerDatabase").d("QueryCallback:: args %s", bindArgs)
                }
            }, Executors.newSingleThreadExecutor())
        }

        return databaseBuilder.build()
    }

    @Provides
    fun foodEntryDao(database: FoodTrackerDatabase) = database.foodEntryDao()
}