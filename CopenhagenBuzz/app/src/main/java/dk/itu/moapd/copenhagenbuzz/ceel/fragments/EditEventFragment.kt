package dk.itu.moapd.copenhagenbuzz.ceel.fragments

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.ceel.R
import dk.itu.moapd.copenhagenbuzz.ceel.data.DataViewModel
import dk.itu.moapd.copenhagenbuzz.ceel.data.Event
import dk.itu.moapd.copenhagenbuzz.ceel.data.EventLocation
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.FragmentEditEventBinding
import dk.itu.moapd.copenhagenbuzz.ceel.fragments.AddEventFragment.Companion
import dk.itu.moapd.copenhagenbuzz.ceel.helpers.DatePickerHelper
import dk.itu.moapd.copenhagenbuzz.ceel.helpers.DropDownHelper
import dk.itu.moapd.copenhagenbuzz.ceel.helpers.LocationHelper
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class EditEventFragment : Fragment() {

    private var _binding: FragmentEditEventBinding? = null
    private val binding get() = requireNotNull(_binding) {
        "Binding is null. Is the view visible?"
    }

    private lateinit var datePickerHelper: DatePickerHelper
    private lateinit var dropdownHelper: DropDownHelper
    private val viewModel: DataViewModel by activityViewModels()

    companion object {
        private const val REQUEST_CAMERA_AND_STORAGE = 100
    }

    // Holds the URI if user chooses/captures a new photo
    private var photoUri: Uri? = null

    // The unique key for the event in Firebase and the current event data.
    private lateinit var eventKey: String
    private lateinit var currentEvent: Event

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentEditEventBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize DatePickerHelper, and attach to the event date field.
        datePickerHelper = DatePickerHelper(requireContext())
        datePickerHelper.attachDatePicker(binding.editTextEventDate)

        // Initialize DropdownHelper and setup dropdown
        dropdownHelper = DropDownHelper(requireContext())
        dropdownHelper.setupEventTypeDropdown(binding.dropdownEventType)

        binding.editEventButtonCapturePhoto.setOnClickListener {
            if (checkPermissions()) {
                launchCamera()
            } else {
                requestPermissions()
            }
        }

        binding.editEventButtonSelectPhoto.setOnClickListener {
            if (checkPermissions()) {
                pickImageLauncher.launch("image/*")
            } else {
                requestPermissions()
            }
        }

        //retrieve eventKey from arguments.
        eventKey = requireArguments().getString("eventKey") ?: ""

        //fetch the event data from our Firebase Realtime Database
        val eventRef = Firebase.database.getReference("copenhagen_buzz/events").child(eventKey)
        eventRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    currentEvent = snapshot.getValue(Event::class.java) ?: throw IllegalStateException("Event data is null")

                    //calling function to prefill the fields for the event, that the user are trying to edit.
                    populateFields(currentEvent)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(view, "Error loading event: ${error.message}", Snackbar.LENGTH_SHORT).show()
            }
        })

        // Set up the save button click listener.
        binding.fabEditEvent.setOnClickListener {
            if (validateInputs()) {
                createUpdatedEvent { updatedEvent ->
                    if (updatedEvent == null) {
                        Snackbar.make(view, "Unable to resolve address. Please check the address.", Snackbar.LENGTH_SHORT).show()
                    } else {
                        //Update the event in Firebase using its unique key.
                        saveEventToDatabase(updatedEvent)
                    }
                }
            } else {
                Snackbar.make(view, "Please fill in all fields.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Pre-fill the UI fields with the event data.
     */
    private fun populateFields(event: Event) {
        binding.editTextEventName.setText(event.eventName)
        binding.editTextEventLocation.setText(event.eventLocation.address)

        //set date
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateString = java.util.Date(currentEvent.eventDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)
        binding.editTextEventDate.setText(dateString)

        //retrieve the photo of this event by using Picasso
        if (event.eventPhotoUrl?.isNotBlank() == true) {
            photoUri = Uri.parse(event.eventPhotoUrl)
            Picasso.get()
                .load(event.eventPhotoUrl)
                .resize(500, 800)
                .rotate(90F)
                .placeholder(R.drawable.baseline_add_photo_alternate_24)
                .into(binding.editEventImagePreview)
        }

        binding.dropdownEventType.setText(event.eventType)
        binding.editTextEventDescription.setText(event.eventDescription)
    }

    /**
     * Checks that none of the fields are empty.
     */
    private fun validateInputs(): Boolean {
        return binding.editTextEventName.text.toString().isNotEmpty() &&
                binding.editTextEventLocation.text.toString().isNotEmpty() &&
                binding.editTextEventDate.text.toString().isNotEmpty() &&
                binding.dropdownEventType.text.toString().isNotEmpty() &&
                binding.editTextEventDescription.text.toString().isNotEmpty()
    }

    /**
     * Converts the user input into an updated Event.
     */
    private fun createUpdatedEvent(callback: (Event?) -> Unit) {
        // Get the updated address string from the EditText.
        val updatedAddress = binding.editTextEventLocation.text.toString()

        // Use the GeocodingHelper to convert the address into coordinates.
        LocationHelper.getCoordinatesFromAddress(requireContext(), updatedAddress) { latitude, longitude ->
            if (latitude == null || longitude == null) {
                callback(null)
            } else {
                // Create a new EventLocation with the updated data.
                val updatedEventLocation = EventLocation(
                    latitude = latitude,
                    longitude = longitude,
                    address = updatedAddress
                )

                // Convert the date string (expected format "yyyy-MM-dd") to a Unix timestamp.
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val eventDate = LocalDate.parse(binding.editTextEventDate.text.toString(), dateFormatter)
                val timestamp = eventDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                // Build the updated Event instance.
                val updatedEvent = Event(
                    eventPhotoUrl = photoUri.toString(),
                    eventName = binding.editTextEventName.text.toString(),
                    eventLocation = updatedEventLocation,
                    eventDate = timestamp,
                    eventType = binding.dropdownEventType.text.toString(),
                    eventDescription = binding.editTextEventDescription.text.toString(),
                    userId = currentEvent.userId
                )
                callback(updatedEvent)
            }
        }
    }

    /**
     * Launcher to capture a photo into our photoUri
     */
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            binding.editEventImagePreview.setImageURI(photoUri)
        } else {
            Snackbar.make(binding.root, "Camera cancelled or failed", Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Launcher to pick an image from gallery
     */
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            photoUri = it
            binding.editEventImagePreview.setImageURI(it)
        }
    }

    /**
     * Creates a Content URI and launches the camera.
     */
    private fun launchCamera() {
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val cv = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "event_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        photoUri = requireContext().contentResolver.insert(collection, cv)
        if (photoUri != null) {
            takePictureLauncher.launch(photoUri)
        } else {
            Snackbar.make(requireView(),"Failed to create image URI", Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Writes the Event under its `eventKey` and then navigates back.
     */
    private fun saveEventToDatabase(event: Event) {
        Firebase.database.getReference("copenhagen_buzz/events")
            .child(eventKey)
            .setValue(event)
            .addOnSuccessListener {
                Snackbar.make(binding.root, "Event updated!", Snackbar.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root, "Update failed: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
    }

    /**
     * Returns true if both CAMERA and READ_EXTERNAL_STORAGE are granted.
     */
    private fun checkPermissions(): Boolean {
        val cam = ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val read = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        return cam && read
    }

    /**
     * Request permissions from the user to use the camera or the library
     */
    private fun requestPermissions() {
        val perms = mutableListOf(Manifest.permission.CAMERA).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        ActivityCompat.requestPermissions(requireActivity(), perms.toTypedArray(), REQUEST_CAMERA_AND_STORAGE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
