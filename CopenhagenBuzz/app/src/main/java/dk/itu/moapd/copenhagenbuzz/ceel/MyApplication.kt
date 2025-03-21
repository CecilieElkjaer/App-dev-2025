package dk.itu.moapd.copenhagenbuzz.ceel

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger


class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply dynamic colors to activities if available.
        DynamicColors.applyToActivitiesIfAvailable(this)
        AppEventsLogger.activateApp(this)
    }
}