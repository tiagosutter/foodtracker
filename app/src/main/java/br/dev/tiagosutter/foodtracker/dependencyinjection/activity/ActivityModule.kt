package br.dev.tiagosutter.foodtracker.dependencyinjection.activity

import android.app.Activity
import android.app.AlarmManager
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import br.dev.tiagosutter.foodtracker.NotificationScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
class ActivityModule {
    @Provides
    fun notificationScheduler(
        activity: AppCompatActivity,
        alarmManager: AlarmManager
    ): NotificationScheduler {
        return NotificationScheduler(activity, alarmManager)
    }


    @Provides
    fun appCompatActivity(activity: Activity): AppCompatActivity = activity as AppCompatActivity

    @Provides
    fun layoutInflater(activity: Activity) = LayoutInflater.from(activity)

    @Provides
    fun fragmentManager(activity: AppCompatActivity) = activity.supportFragmentManager
}