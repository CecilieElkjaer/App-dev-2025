package dk.itu.moapd.copenhagenbuzz.ceel.fragments

import android.Manifest
import android.content.ContentValues
import android.net.Uri
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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import dk.itu.moapd.copenhagenbuzz.ceel.data.DataViewModel
import dk.itu.moapd.copenhagenbuzz.ceel.data.Event
import dk.itu.moapd.copenhagenbuzz.ceel.data.EventLocation
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.FragmentAddEventBinding
import dk.itu.moapd.copenhagenbuzz.ceel.helpers.DatePickerHelper
import dk.itu.moapd.copenhagenbuzz.ceel.helpers.DropDownHelper
import dk.itu.moapd.copenhagenbuzz.ceel.helpers.LocationHelper
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import android.content.pm.PackageManager
import android.os.Build
import com.google.firebase.storage.storage

/**
 * A simple [Fragment] subclass.
 * Use the [AddEventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEventFragment : Fragment() {

    private var _binding: FragmentAddEventBinding? = null
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private lateinit var datePickerHelper: DatePickerHelper
    private lateinit var dropdownHelper: DropDownHelper
    private val viewModel: DataViewModel by activityViewModels()
    companion object {
        private const val REQUEST_CAMERA_AND_STORAGE = 100
    }
    // Holds the URI of the photo to upload later
    private var photoUri: Uri? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = FragmentAddEventBinding.inflate(inflater, container, false).also {
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

        binding.buttonCapturePhoto.setOnClickListener {
            if (checkPermissions()) {
                launchCamera()
            } else {
                requestPermissions()
            }
        }

        binding.buttonSelectPhoto.setOnClickListener {
            if (checkPermissions()) {
                pickImageLauncher.launch("image/*")
            } else {
                requestPermissions()
            }
        }

        //clicking the add event button, propagates the following
        binding.fabAddEvent.setOnClickListener {
            if (validateInputs()) {
                val inputAddress = binding.editTextEventLocation.text.toString()

                //uses GeocodingHelper to convert the address into coordinates.
                LocationHelper.getCoordinatesFromAddress(requireContext(), inputAddress) { latitude, longitude ->
                    if (latitude == null || longitude == null) {
                        Snackbar.make(requireView(), "Unable to resolve address. Please check the address.", Snackbar.LENGTH_SHORT).show()
                    } else {
                        //create an EventLocation instance with the retrieved coordinates.
                        val eventLocation = EventLocation(latitude = latitude, longitude = longitude, address = inputAddress)

                        //Reserve a new key in the Realtime Database
                        val eventsRef = Firebase.database.getReference("copenhagen_buzz/events")
                        val newEventRef = eventsRef.push()
                        val eventKey = newEventRef.key ?: run {
                            Snackbar.make(requireView(), "Failed to generate event key", Snackbar.LENGTH_SHORT).show()
                            return@getCoordinatesFromAddress
                        }

                        //Upload the photo to the Firebase Storage under "events/<eventKey>.jpg"
                        val photo = photoUri
                        val storageRef = Firebase.storage.reference.child("events").child("$eventKey.jpg")

                        if (photo != null) {
                            storageRef.putFile(photo).continueWithTask{ uploadTask ->
                                if (!uploadTask.isSuccessful) throw uploadTask.exception!!
                                storageRef.downloadUrl
                            }.addOnSuccessListener { photoUrl ->
                                //create the event with the remote URL
                                val event = createEvent(eventLocation, photoUrl)

                                //persist the event in the database
                                newEventRef.setValue(event).addOnFailureListener { error ->
                                    Snackbar.make(binding.root, "Save failed: ${error.message}", Snackbar.LENGTH_LONG).show()
                                }
                                Snackbar.make(binding.root, "Event added!", Snackbar.LENGTH_SHORT).show()
                                requireActivity().supportFragmentManager.popBackStack()
                            }.addOnFailureListener { error ->
                                Snackbar.make(requireView(), "Photo upload failed: ${error.message}", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else {
                Snackbar.make(requireView(), "Please fill in all fields", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Input fields are being validated to check if user has inputted all the needed information for the event.
     */
    private fun validateInputs(): Boolean {
        return binding.editTextEventName.text.toString().isNotEmpty() &&
                binding.editTextEventLocation.text.toString().isNotEmpty() &&
                binding.editTextEventDate.text.toString().isNotEmpty() &&
                binding.dropdownEventType.text.toString().isNotEmpty() &&
                binding.editTextEventDescription.text.toString().isNotEmpty()
    }

    /**
     * Creates the event with needed information
     */
    private fun createEvent(location: EventLocation, photoUrl: Uri?): Event {
        //convert date string to Long
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val eventDate = LocalDate.parse(binding.editTextEventDate.text.toString(), dateFormatter)
        val timestamp = eventDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        //retrieve user id
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        //create an Event object
        return Event(
            eventPhotoUrl = photoUrl?.toString(),
            eventName = binding.editTextEventName.text.toString(),
            eventLocation = location,
            eventDate = timestamp,
            eventType = binding.dropdownEventType.text.toString(),
            eventDescription = binding.editTextEventDescription.text.toString(),
            userId = userId
        )
    }

    /**
     * Creates a Content URI and launches the camera.
     */
    private fun launchCamera() {
        val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val cv = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME,"event_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg")
        }
        photoUri = requireContext().contentResolver.insert(imageCollection, cv)
        if (photoUri != null) {
            takePictureLauncher.launch(photoUri)
        } else {
            Snackbar.make(requireView(),"Failed to create image URI", Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Launcher to capture a photo into our photoUri
     */
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            binding.imagePreview?.setImageURI(photoUri)
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
            binding.imagePreview.setImageURI(it)
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
        val perms = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms += Manifest.permission.READ_MEDIA_IMAGES
        } else {
            perms += Manifest.permission.READ_EXTERNAL_STORAGE
        }
        ActivityCompat.requestPermissions(requireActivity(), perms.toTypedArray(), REQUEST_CAMERA_AND_STORAGE)
    }

    /**
     * Handle the user’s response to the permission request
     * */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_AND_STORAGE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted – ask user to tap again
                Snackbar.make(binding.root, "Permissions granted, please tap again", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.root, "Camera & storage permissions are required", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}