package br.dev.tiagosutter.foodtracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity


class NotificationScheduler(
    private val activity: AppCompatActivity,
    private val alarmManager: AlarmManager
) {

    companion object {
        const val CHANNEL_ID = "recurring_alerts"
        const val CHANNEL_NAME = "Daily Alerts"
    }

    fun schedule() {
        val intent = Intent(
            activity,
            ScheduleNotificationBroadcastReceiver::class.java
        )
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(activity, 1, intent, flags)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            AlarmManager.INTERVAL_HALF_DAY,
            AlarmManager.INTERVAL_HALF_DAY,
            pendingIntent
        )
    }
}