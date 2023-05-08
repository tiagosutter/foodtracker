package br.dev.tiagosutter.foodtracker.dependencyinjection.app

import android.app.AlarmManager
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.room.RoomDatabase
import br.dev.tiagosutter.foodtracker.BuildConfig
import br.dev.tiagosutter.foodtracker.database.FoodTrackerDatabase
import br.dev.tiagosutter.foodtracker.notifications.NotificationScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
    fun notificationScheduler(
        @ApplicationContext context: Context,
        alarmManager: AlarmManager
    ): NotificationScheduler {
        return NotificationScheduler(context, alarmManager)
    }

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