package br.dev.tiagosutter.foodtracker.logging

import android.util.Log.ERROR
import br.dev.tiagosutter.foodtracker.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber


fun setupTimber() {
    if (BuildConfig.DEBUG) {
        Timber.plant(object : Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String? {
                return "(${element.fileName}:${element.lineNumber})#${element.methodName}"
            }
        })
    } else {
        Timber.plant(CrashlyticsReportingTree())
    }
}


private class CrashlyticsReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log(message)

        if (priority == ERROR) {
            if (t == null) crashlytics.recordException(Throwable(message))
            else crashlytics.recordException(t)
        }
    }
}