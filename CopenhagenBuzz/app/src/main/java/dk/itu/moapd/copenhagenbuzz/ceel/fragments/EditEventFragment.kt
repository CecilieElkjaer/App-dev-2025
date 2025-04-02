package dk.itu.moapd.copenhagenbuzz.ceel.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.ceel.R
import dk.itu.moapd.copenhagenbuzz.ceel.data.DataViewModel
import dk.itu.moapd.copenhagenbuzz.ceel.data.Event
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.FragmentEditEventBinding
import dk.itu.moapd.copenhagenbuzz.ceel.helpers.DatePickerHelper
import dk.itu.moapd.copenhagenbuzz.ceel.helpers.DropDownHelper
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

        // Retrieve eventKey and currentEvent from arguments.
        // This assumes that the Event class implements Parcelable.
        eventKey = requireArguments().getString("eventKey") ?: ""

        // Fetch the event data from our Firebase Realtime Database
        val eventRef = Firebase.database.getReference("copenhagen_buzz/events").child(eventKey)
        eventRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    currentEvent = snapshot.getValue(Event::class.java) ?: throw IllegalStateException("Event data is null")

                    //calling function to prefill the fields for the event, that the user are trying to edit.
                    populateFields(currentEvent)
                } else {
                    Snackbar.make(view, "Event not found.", Snackbar.LENGTH_SHORT).show()
                    // Optionally, navigate back.
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(view, "Error loading event: ${error.message}", Snackbar.LENGTH_SHORT)
                    .show()
            }
        })

        // Set up the save button click listener.
        binding.fabEditEvent.setOnClickListener {
            if (validateInputs()) {
                val updatedEvent = createUpdatedEvent()

                // Update the event in Firebase using its unique key.
                Firebase.database.getReference("copenhagen_buzz/events")
                    .child(eventKey)
                    .setValue(updatedEvent)
                    .addOnSuccessListener {
                        Snackbar.make(view, "Event updated successfully.", Snackbar.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                    .addOnFailureListener { error ->
                        Snackbar.make(view, "Failed to update event: ${error.message}", Snackbar.LENGTH_SHORT).show()
                    }
            } else {
                Snackbar.make(view, "Please fill in all fields.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    //Pre-fill the UI fields with the event data.
    private fun populateFields(event: Event) {
        binding.editTextEventName.setText(event.eventName)
        binding.editTextEventLocation.setText(event.eventLocation)

        //set date
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateString = java.util.Date(currentEvent.eventDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)
        binding.editTextEventDate.setText(dateString)

        binding.dropdownEventType.setText(event.eventType)
        binding.editTextEventDescription.setText(event.eventDescription)
    }

    // Checks that none of the fields are empty.
    private fun validateInputs(): Boolean {
        return binding.editTextEventName.text.toString().isNotEmpty() &&
                binding.editTextEventLocation.text.toString().isNotEmpty() &&
                binding.editTextEventDate.text.toString().isNotEmpty() &&
                binding.dropdownEventType.text.toString().isNotEmpty() &&
                binding.editTextEventDescription.text.toString().isNotEmpty()
    }

    // Converts the user input into an updated Event object.
    private fun createUpdatedEvent(): Event {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val eventDate = LocalDate.parse(binding.editTextEventDate.text.toString(), dateFormatter)
        val timestamp = eventDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()

        return Event(
            eventPhoto = currentEvent.eventPhoto, // Retain existing photo or update if needed.
            eventName = binding.editTextEventName.text.toString(),
            eventLocation = binding.editTextEventLocation.text.toString(),
            eventDate = timestamp,
            eventType = binding.dropdownEventType.text.toString(),
            eventDescription = binding.editTextEventDescription.text.toString(),
            userId = currentEvent.userId
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
