package dk.itu.moapd.copenhagenbuzz.ceel.helpers

import android.app.Activity
import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.io.IOException
import java.util.Locale

object LocationHelper {
    fun getAddressFromCoordinates(context: Context, latitude: Double, longitude: Double, callback: (String) -> Unit) {
        Thread {
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                // Perform reverse geocoding and use let to handle the non-null list result.
                geocoder.getFromLocation(latitude, longitude, 1)?.let { addresses ->
                    val address = if (addresses.isNotEmpty()) {
                        addresses[0].getAddressLine(0)
                    } else {
                        "Address not available"
                    }
                    // Return the result on the UI thread.
                    (context as? Activity)?.runOnUiThread {
                        callback(address)
                    } ?: callback(address)
                } ?: run {
                    (context as? Activity)?.runOnUiThread {
                        callback("Address not available")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                (context as? Activity)?.runOnUiThread {
                    callback("Address not available")
                }
            }
        }.start()
    }

    /**
     * Uses forward geocoding to convert a street address into coordinates.
     *
     * @param context The context used to instantiate the Geocoder.
     * @param address The street address input by the user.
     * @param callback A lambda that receives the latitude and longitude values.
     *                 If geocoding fails, both values will be null.
     */
    fun getCoordinatesFromAddress(context: Context, address: String, callback: (latitude: Double?, longitude: Double?) -> Unit) {
        Thread {
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                // Attempt to get a list of addresses matching the provided address string.
                geocoder.getFromLocationName(address, 1)?.let { addressList ->
                    if (addressList.isNotEmpty()) {
                        val location = addressList[0]
                        val lat = location.latitude
                        val lng = location.longitude
                        (context as? Activity)?.runOnUiThread {
                            callback(lat, lng)
                        } ?: callback(lat, lng)
                    } else {
                        (context as? Activity)?.runOnUiThread {
                            callback(null, null)
                        } ?: callback(null, null)
                    }
                } ?: run {
                    (context as? Activity)?.runOnUiThread {
                        callback(null, null)
                    } ?: callback(null, null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                (context as? Activity)?.runOnUiThread {
                    callback(null, null)
                } ?: callback(null, null)
            }
        }.start()
    }
}