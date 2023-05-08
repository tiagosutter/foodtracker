package br.dev.tiagosutter.foodtracker.ui.settings

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.preference.SwitchPreferenceCompat
import br.dev.tiagosutter.foodtracker.R
import br.dev.tiagosutter.foodtracker.notifications.NotificationScheduler
import br.dev.tiagosutter.foodtracker.settings.PreferenceStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var perfStore: PreferenceStore

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val preference: SwitchPreferenceCompat? = findPreference<SwitchPreferenceCompat>("notify_daily")

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    preference?.isChecked = true
                    notificationScheduler.schedule()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.could_not_enable_notifications_permission),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        val preferenceChangeListener = Preference.OnPreferenceChangeListener { _, notify ->
            notify as Boolean
            if (!notify) {
                notificationScheduler.cancel()
                true
            } else {
                val schedulingResult = notificationScheduler.schedule()

                if (schedulingResult == NotificationScheduler.SchedulingResult.NO_PERMISSION) {
                    requestNotificationPermission(requestPermissionLauncher)
                    false
                } else {
                    true
                }
            }
        }

        preference?.onPreferenceChangeListener = preferenceChangeListener
    }

    private fun requestNotificationPermission(requestPermissionLauncher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}