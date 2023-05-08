package br.dev.tiagosutter.foodtracker.settings

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferenceStore @Inject constructor(@ApplicationContext applicationContext: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

    fun getDailyNotificationsEnabled(): Boolean {
        return prefs.getBoolean("notify_daily", false)
    }

    fun enableNotifications() {
        prefs.edit {
            putBoolean("notify_daily", true)
        }
    }

    fun disabledNotifications() {
        prefs.edit {
            putBoolean("notify_daily", false)
        }
    }
}