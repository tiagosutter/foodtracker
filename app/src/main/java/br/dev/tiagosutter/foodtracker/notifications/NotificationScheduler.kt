package br.dev.tiagosutter.foodtracker.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


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
            ContextCompat.checkSelfPermission(
                context,
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

        val pendingIntent = createIntent()
        alarmManager.setInexactRepeating(
            AlarmManager.RTC,
            System.currentTimeMillis() + AlarmManager.INTERVAL_HALF_DAY,
            AlarmManager.INTERVAL_HALF_DAY,
            pendingIntent
        )

        return SchedulingResult.SUCCESS
    }

    fun cancel() {
        val pendingIntent = createIntent()
        pendingIntent?.cancel()
        alarmManager.cancel(pendingIntent)
    }

    private fun createIntent(): PendingIntent? {
        val intent = Intent(
            context,
            ScheduleNotificationBroadcastReceiver::class.java
        )
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, 1, intent, flags)
    }
}