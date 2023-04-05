package br.dev.tiagosutter.foodtracker

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

class TestingDependencyInjection @Inject constructor(private val activity: AppCompatActivity) {
    private val logTag = "TestingDependencyInject"

    fun logSomeString() {
        Log.d(logTag, activity.getString(R.string.app_name))
        Toast.makeText(activity, "Test", Toast.LENGTH_SHORT).show()
    }
}