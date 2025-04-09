package dk.itu.moapd.copenhagenbuzz.ceel

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.database.FirebaseDatabase
import io.github.cdimascio.dotenv.Dotenv


class MyApplication: Application() {
    companion object {
        lateinit var DATABASE_URL: String
        lateinit var database: FirebaseDatabase
    }

    override fun onCreate() {
        super.onCreate()

        // Load the .env file
        //val dotenv = Dotenv.configure().directory("CopenhagenBuzz/app/src/").filename(".env").load()
        //DATABASE_URL = dotenv["DATABASE_URL"] ?: "https://moapd25-default-rtdb.europe-west1.firebasedatabase.app"

        DATABASE_URL = "https://moapd25-default-rtdb.europe-west1.firebasedatabase.app"

        database = FirebaseDatabase.getInstance()
        database.setPersistenceEnabled(true)

        val databaseReference = database.reference
        databaseReference.keepSynced(true)

        // Apply dynamic colors to activities if available.
        DynamicColors.applyToActivitiesIfAvailable(this)
        AppEventsLogger.activateApp(this)
    }
}