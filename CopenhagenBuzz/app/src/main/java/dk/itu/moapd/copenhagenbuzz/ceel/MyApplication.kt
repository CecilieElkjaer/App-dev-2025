package dk.itu.moapd.copenhagenbuzz.ceel

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.database.FirebaseDatabase

class MyApplication: Application() {
    companion object {
        lateinit var DATABASE_URL: String
        lateinit var database: FirebaseDatabase
    }

    override fun onCreate() {
        super.onCreate()

        DATABASE_URL = BuildConfig.DATABASE_URL

        database = FirebaseDatabase.getInstance()
        database.setPersistenceEnabled(true)

        val databaseReference = database.reference
        databaseReference.keepSynced(true)

        // Apply dynamic colors to activities if available.
        DynamicColors.applyToActivitiesIfAvailable(this)
        AppEventsLogger.activateApp(this)
        SharedPreferenceUtil.saveLocationTrackingPref(this, false)
    }
}