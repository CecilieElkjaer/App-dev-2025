package dk.itu.moapd.copenhagenbuzz.ceel.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Build.VERSION
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dk.itu.moapd.copenhagenbuzz.ceel.R
import dk.itu.moapd.copenhagenbuzz.ceel.SharedPreferenceUtil
import dk.itu.moapd.copenhagenbuzz.ceel.SharedPreferenceUtil.toSimpleDateFormat
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.FragmentFavoritesBinding
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.FragmentMapsBinding
import dk.itu.moapd.copenhagenbuzz.ceel.services.LocationService
import java.security.AccessController.checkPermission
import java.util.Locale

class MapsFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    /**
     * Receiver for location broadcasts from [LocationService].
     */
    private inner class LocationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                intent.getParcelableExtra(LocationService.EXTRA_LOCATION, Location::class.java)
            else
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(LocationService.EXTRA_LOCATION)
            location?.let {
                updateLocationDetails(it)
            }
        }
    }

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = requireNotNull(_binding) {
        "Cannot access binding because it is null. Is the view visible?"
    }

    /**
     * A set of private constants used in this class.
     */
    companion object {
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    }

    /**
     * The SharedPreferences instance that can be used to save and retrieve data.
     */
    private lateinit var sharedPreferences: SharedPreferences

    /**
     * Receiver for location broadcasts from [LocationService].
     */
    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver

    /**
     * Provides location updates for while-in-use feature.
     */
    private var locationService: LocationService? = null

    /**
     * A flag to indicate whether a bound to the service.
     */
    private var locationServiceBound = false

    /**
     * Defines callbacks for service binding, passed to `bindService()`.
     */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.service
            locationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationService = null
            locationServiceBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentMapsBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the SharedPreferences instance.
        sharedPreferences = requireActivity()
            .getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        // Initialize the broadcast receiver.
        locationBroadcastReceiver = LocationBroadcastReceiver()

        // Define the UI behavior using lambda expressions.
        binding.buttonState?.setOnClickListener {
            sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false).let { enabled ->
                if (enabled) {
                    resetLocationDetails()
                    locationService?.unsubscribeToLocationUpdates()
                } else {
                    if (checkPermission()) {
                        locationService?.subscribeToLocationUpdates()
                    } else {
                        requestUserPermissions()
                    }
                }
            }
        }
    }

    /**
     * Called when the Fragment is visible to the user. This is generally tied to
     * `Activity.onStart()` of the containing Activity's lifecycle.
     */
    override fun onStart() {
        super.onStart()

        // Update the UI to reflect the state of the service.
        updateButtonState(
            sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
        )

        // Register the shared preference change listener.
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        // Bind to the service.
        Intent(requireContext(), LocationService::class.java).let { serviceIntent ->
            requireActivity().bindService(
                serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * Called when the fragment is visible to the user and actively running. Tied
     * to `Activity.onResume()` of the containing Activity's lifecycle.
     */
    override fun onResume() {
        super.onResume()

        // Register the broadcast receiver.
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationBroadcastReceiver,
            IntentFilter(LocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )
    }

    /**
     * Called when the Fragment is no longer resumed. Tied to `Activity.onPause()`
     * of the containing Activity's lifecycle.
     */
    override fun onPause() {
        // Unregister the broadcast receiver.
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(
            locationBroadcastReceiver
        )
        super.onPause()
    }

    /**
     * Called when the Fragment is no longer started. Tied to `Activity.onStop()`
     * of the containing Activity's lifecycle.
     */
    override fun onStop() {
        // Unbind from the service.
        if (locationServiceBound) {
            requireActivity().unbindService(serviceConnection)
            locationServiceBound = false
        }

        // Unregister the shared preference change listener.
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    /**
     * Called when the view has been detached from the fragment. The next time the fragment needs to be displayed, a new view will be created.
     * Called after `onStop()` and before `onDestroy()`.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Checks if the user allows the application to use all location-aware resources to
     * monitor the user's location.
     *
     * @return A boolean value with the user permission agreement.
     */
    private fun checkPermission() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Create a set of dialogs to show to the users and ask them for permissions to get the device's
     * resources.
     */
    private fun requestUserPermissions() {
        if (!checkPermission())
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
    }

    /**
     * Called when a shared preference is changed, added, or removed. This may be called even if a
     * preference is set to its existing value. This callback will be run on the main thread.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED)
            sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
                .let(::updateButtonState)
    }

    /**
     * Updates the text of the button based on the `trackingLocation` state.
     *
     * @param trackingLocation Boolean indicating whether the location tracking is active or not.
     */
    private fun updateButtonState(trackingLocation: Boolean) {
        val buttonText = if (trackingLocation) R.string.button_stop else R.string.button_start
        binding.buttonState?.text = getString(buttonText)
    }

    /**
     * Updates the location details into the UI components. Sets the latitude, longitude, altitude,
     * and speed in the respective EditTexts.
     *
     * @param location The location to be updated in the UI components.
     */
    private fun updateLocationDetails(location: Location) {
        with(binding) {
            // Fill the event details into the UI components.
            editTextLatitude?.setText(
                String.format(Locale.getDefault(), "%.6f", location.latitude)
            )
            editTextLongitude?.setText(
                String.format(Locale.getDefault(), "%.6f", location.longitude)
            )
            editTextAltitude?.setText(
                String.format(Locale.getDefault(), "%.6f", location.altitude)
            )
            editTextSpeed?.setText(
                getString(R.string.text_speed_km, location.speed.toInt())
            )
            editTextTime?.setText(location.time.toSimpleDateFormat())
        }
    }

    /**
     * Resets the location details into the UI components. Sets the latitude, longitude, altitude,
     * and speed in the respective EditTexts.
     */
    private fun resetLocationDetails() {
        with(binding) {
            // Fill the event details into the UI components.
            editTextLatitude?.setText(getString(R.string.text_not_available))
            editTextLongitude?.setText(getString(R.string.text_not_available))
            editTextAltitude?.setText(getString(R.string.text_not_available))
            editTextSpeed?.setText(getString(R.string.text_not_available))
            editTextTime?.setText(getString(R.string.text_not_available))
        }
    }
}