package br.dev.tiagosutter.foodtracker.ui.settings

import android.Manifest
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import br.dev.tiagosutter.foodtracker.R
import br.dev.tiagosutter.foodtracker.notifications.NotificationScheduler
import br.dev.tiagosutter.foodtracker.settings.PreferenceStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var perfStore: PreferenceStore

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val preference: SwitchPreferenceCompat? =
            findPreference<SwitchPreferenceCompat>("notify_daily")

        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    return@registerForActivityResult
                }
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.POST_NOTIFICATIONS
                )
                if (isGranted) {
                    preference?.isChecked = true
                    notificationScheduler.schedule()
                } else if (shouldShowRationale) {
                    AlertDialog.Builder(requireContext())
                        .setNegativeButton(R.string.retry) { dialog, _ ->
                            requestNotificationPermission(requestPermissionLauncher)
                            dialog.dismiss()
                        }
                        .setPositiveButton(R.string.i_am_sure) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setTitle(R.string.notification_permission)
                        .setMessage(R.string.are_you_sure_no_notification_permission)
                        .show()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.notification_permission)
                        .setMessage(R.string.could_not_enable_notifications_permission)
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
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
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.POST_NOTIFICATIONS
            )
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}