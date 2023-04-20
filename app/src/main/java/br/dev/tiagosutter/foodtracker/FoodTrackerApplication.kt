package br.dev.tiagosutter.foodtracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FoodTrackerApplication : Application() {

    override fun onCreate() {
        val channelId = NotificationScheduler.CHANNEL_ID
        val channelName = NotificationScheduler.CHANNEL_NAME
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationManagerCompat = NotificationManagerCompat.from(this)
            // Notification channels are only for APIs 26 and above
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManagerCompat.createNotificationChannel(channel)
        }

        super.onCreate()
    }

}