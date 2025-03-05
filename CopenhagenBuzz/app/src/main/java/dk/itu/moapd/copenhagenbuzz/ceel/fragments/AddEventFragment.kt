package dk.itu.moapd.copenhagenbuzz.ceel.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.ceel.data.Event
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.FragmentAddEventBinding
import dk.itu.moapd.copenhagenbuzz.ceel.helpers.DatePickerHelper
import dk.itu.moapd.copenhagenbuzz.ceel.helpers.DropDownHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * A simple [Fragment] subclass.
 * Use the [AddEventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEventFragment : Fragment() {

    private var _binding: FragmentAddEventBinding? = null
    private val binding get() = requireNotNull(_binding) {
        "Cannot access binding because it is null. Is the view visible?"
    }
    private lateinit var datePickerHelper: DatePickerHelper
    private lateinit var dropdownHelper: DropDownHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentAddEventBinding.inflate(inflater, container, false).also {
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

        binding.fabAddEvent.setOnClickListener{
            if(validateInputs()) {
                val event = createEvent()
                Snackbar.make(requireView(), "Event '${event.eventName}' added!\"", Snackbar.LENGTH_SHORT).show()

                requireActivity().supportFragmentManager.popBackStack()
            } else {
                Snackbar.make(requireView(), "Please fill in all fields", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(): Boolean {
        return binding.editTextEventDate.text.toString().isNotEmpty() &&
                binding.editTextEventLocation.text.toString().isNotEmpty() &&
                binding.editTextEventDate.text.toString().isNotEmpty() &&
                binding.dropdownEventType.text.toString().isNotEmpty() &&
                binding.editTextEventDescription.text.toString().isNotEmpty()
    }

    private fun createEvent(): Event {
        // Convert date string to LocalDate
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val eventDate = LocalDate.parse(binding.editTextEventDate.text.toString(), dateFormatter)

        // Create an Event object
        return Event(
            eventPhoto = "https://source.unsplash.com/random/800x600",
            eventName = binding.editTextEventName.text.toString(),
            eventLocation = binding.editTextEventLocation.text.toString(),
            eventDate = eventDate,
            eventType = binding.dropdownEventType.text.toString(),
            eventDescription = binding.editTextEventDescription.text.toString()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}