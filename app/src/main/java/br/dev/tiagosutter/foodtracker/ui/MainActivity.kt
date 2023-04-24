package br.dev.tiagosutter.foodtracker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import br.dev.tiagosutter.foodtracker.notifications.NotificationScheduler
import br.dev.tiagosutter.foodtracker.R
import br.dev.tiagosutter.foodtracker.databinding.ActivityMainBinding
import br.dev.tiagosutter.foodtracker.ui.entrieslist.FoodEntriesFragmentDirections
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeFirebaseAnalytics()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.FoodEntriesListFragment -> {
                    supportActionBar?.hide()
                    binding.fab.isEnabled = true
                    binding.fab.show()
                }
                else -> {
                    supportActionBar?.show()
                    binding.fab.hide()
                }
            }
        }


        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener {
            it.isEnabled = false
            val action = FoodEntriesFragmentDirections
                .actionFoodEntriesListFragmentToNewFoodEntryFragment(null, "")
            navController.navigate(action)
        }

        if (!hasNotificationPermission() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        } else {
            notificationScheduler.schedule()
        }
    }

    private fun initializeFirebaseAnalytics() {
        // Initialize Firebase Analytics so it starts automatically logging
        //  some events and user properties like
        //   https://support.google.com/firebase/answer/9234069?hl=en
        //   https://support.google.com/firebase/answer/9268042?hl=en
        analytics = Firebase.analytics
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    notificationScheduler.schedule()

                } else {
                    // TODO: Create a settings screen for
                    //  Explaining to the user that the feature is unavailable because the
                    //  feature requires a permission that the user has denied. At the
                    //  same time, respect the user's decision. Don't link to system
                    //  settings in an effort to convince the user to change their
                    //  decision.
                }
            }
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        return if (onBackPressedDispatcher.hasEnabledCallbacks()) {
            onBackPressedDispatcher.onBackPressed()
            true
        } else {
            navController.navigateUp() || super.onSupportNavigateUp()
        }
    }
}