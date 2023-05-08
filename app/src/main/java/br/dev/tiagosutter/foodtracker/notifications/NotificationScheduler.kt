package br.dev.tiagosutter.foodtracker.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class NotificationScheduler(
    private val context: Context,
    private val alarmManager: AlarmManager
) {

    companion object {
        const val CHANNEL_ID = "recurring_alerts"
        const val CHANNEL_NAME = "Daily Alerts"
    }

    enum class SchedulingResult {
        SUCCESS,
        NO_PERMISSION
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun schedule(): SchedulingResult {
        val noPermission = !hasNotificationPermission()
        if (noPermission) {
            return SchedulingResult.NO_PERMISSION
        }

        val intent = Intent(
            context,
            ScheduleNotificationBroadcastReceiver::class.java
        )
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 1, intent, flags)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC,
            System.currentTimeMillis() + AlarmManager.INTERVAL_HALF_DAY,
            AlarmManager.INTERVAL_HALF_DAY,
            pendingIntent
        )

        return SchedulingResult.SUCCESS
    }
}