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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import dk.itu.moapd.copenhagenbuzz.ceel.R
import dk.itu.moapd.copenhagenbuzz.ceel.SharedPreferenceUtil
import dk.itu.moapd.copenhagenbuzz.ceel.SharedPreferenceUtil.toSimpleDateFormat
import dk.itu.moapd.copenhagenbuzz.ceel.data.Event
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.FragmentFavoritesBinding
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.FragmentMapsBinding
import dk.itu.moapd.copenhagenbuzz.ceel.services.LocationService
import java.security.AccessController.checkPermission
import java.util.Locale

class MapsFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener, OnMapReadyCallback {
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

    private lateinit var googleMap: GoogleMap
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver
    private var locationService: LocationService? = null
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentMapsBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the SharedPreferences instance.
        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

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

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    /**
     * Called when the GoogleMap is ready. Implements teacher's OnMapReadyCallback.
     */
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Add a marker for IT University of Copenhagen (teacher's sample)
        val itu = LatLng(55.6596, 12.5910)
        googleMap.addMarker(MarkerOptions().position(itu).title("IT University of Copenhagen"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itu, 12f))
        googleMap.setPadding(0, 100, 0, 0)

        // Enable MyLocation layer if permission is granted, else request it.
        if (checkPermission()) {
            googleMap.isMyLocationEnabled = true
        } else {
            requestUserPermissions()
        }

        // Now load event markers from your database.
        loadEventMarkers()

        // Set marker click listener to display event details.
        googleMap.setOnMarkerClickListener { marker ->
            val event = marker.tag as? Event
            event?.let {
                Snackbar.make(requireView(), "Event: ${it.eventName}\nType: ${it.eventType}", Snackbar.LENGTH_LONG).show()
            }
            true // Consume the event.
        }
    }

    /**
     * Load event markers from Firebase and add them to the GoogleMap.
     */
    private fun loadEventMarkers() {
        val eventsRef = Firebase.database.getReference("copenhagen_buzz/events")
        eventsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { child ->
                    child.getValue(Event::class.java)?.let { event ->
                        val position = LatLng(event.eventLocation.latitude, event.eventLocation.longitude)
                        val marker = googleMap.addMarker(
                            MarkerOptions().position(position).title(event.eventName)
                                .snippet(event.eventType)
                        )
                        // Use the marker's tag to store the event so that it can be retrieved on click.
                        marker?.tag = event
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Optionally, show an error message.
            }
        })
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
            editTextLatitude?.setText(String.format(Locale.getDefault(), "%.6f", location.latitude))
            editTextLongitude?.setText(String.format(Locale.getDefault(), "%.6f", location.longitude))
            editTextAltitude?.setText(String.format(Locale.getDefault(), "%.6f", location.altitude))
            editTextSpeed?.setText(getString(R.string.text_speed_km, location.speed.toInt()))
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