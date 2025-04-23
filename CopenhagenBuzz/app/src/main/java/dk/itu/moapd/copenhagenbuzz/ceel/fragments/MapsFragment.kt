package dk.itu.moapd.copenhagenbuzz.ceel.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
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
import dk.itu.moapd.copenhagenbuzz.ceel.data.Event
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.FragmentMapsBinding
import dk.itu.moapd.copenhagenbuzz.ceel.services.LocationService

class MapsFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener, OnMapReadyCallback {
    /**
     * Receiver for location broadcasts from [LocationService].
     */
    private inner class LocationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                intent.getParcelableExtra(LocationService.EXTRA_LOCATION, Location::class.java)
            else
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(LocationService.EXTRA_LOCATION)
            location?.let {
                updateLocation(it)
            }
        }
    }

    private lateinit var googleMap: GoogleMap
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = requireNotNull(_binding) {
        "Cannot access binding because it is null. Is the view visible?"
    }

    //broadcastReceiver to receive location updates from LocationService.
    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver
    private lateinit var sharedPreferences: SharedPreferences
    var hasCenteredMap = false

    //bind to the LocationService (retaining your location update functionality)
    private var locationService: LocationService? = null
    private var locationServiceBound = false

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

    /**
     * A set of private constants used in this class.
     */
    companion object {
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentMapsBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Initialize SharedPreferences.
        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        //Set up the location broadcast receiver.
        locationBroadcastReceiver = LocationBroadcastReceiver()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    /**
     * Called when the GoogleMap is ready. Implements teacher's OnMapReadyCallback.
     */
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Add a marker for IT University of Copenhagen
        val itu = LatLng(55.6596, 12.5910)
        googleMap.addMarker(MarkerOptions().position(itu).title("IT University of Copenhagen"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itu, 12f))
        googleMap.setPadding(0, 100, 0, 0)

        googleMap.uiSettings.apply {
            isScrollGesturesEnabled = true
            isZoomGesturesEnabled = true
            isRotateGesturesEnabled = true
            isTiltGesturesEnabled = true
            isZoomControlsEnabled = true
        }

        //enable MyLocation layer if permission is granted, else request it.
        if (checkPermission()) {
            googleMap.isMyLocationEnabled = true
        } else {
            requestUserPermissions()
        }

        //load event markers from the firebase database.
        loadEventMarkers()

        //Set marker click listener to display event details.
        googleMap.setOnMarkerClickListener { marker ->
            val event = marker.tag as? Event
            event?.let {
                Snackbar.make(requireView(), "Event: ${it.eventName}\nType: ${it.eventType}", Snackbar.LENGTH_LONG).show()
            }
            true
        }
    }


    /**
     * Load event markers from Firebase and add them to the GoogleMap on MapsFragment page.
     */
    private fun loadEventMarkers() {
        val eventsRef = Firebase.database.getReference("copenhagen_buzz/events")
        eventsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { child ->
                    child.getValue(Event::class.java)?.let { event ->
                        val position = LatLng(event.eventLocation.latitude, event.eventLocation.longitude)
                        val marker = googleMap.addMarker(
                            MarkerOptions().position(position).title(event.eventName).snippet(event.eventType)
                        )
                        //uses the marker's tag to store the event so that it can be retrieved on click.
                        marker?.tag = event
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Update to the location of the user
     */
    private fun updateLocation(location: Location) {
        if (!hasCenteredMap) {
            val userLatLng = LatLng(location.latitude, location.longitude)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
            hasCenteredMap = true
        }
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

    override fun onStart() {
        super.onStart()
        // Register SharedPreferences change listener.
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        // Bind to the LocationService.
        Intent(requireContext(), LocationService::class.java).let { intent ->
            requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onResume() {
        super.onResume()
        // Register the location broadcast receiver.
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationBroadcastReceiver,
            android.content.IntentFilter(LocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)
        super.onPause()
    }

    override fun onStop() {
        if (locationServiceBound) {
            requireActivity().unbindService(serviceConnection)
            locationServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        // No specific UI update needed in the current map-only layout.
    }
}