package dk.itu.moapd.copenhagenbuzz.ceel.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import com.firebase.ui.database.FirebaseListOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.ceel.R
import dk.itu.moapd.copenhagenbuzz.ceel.data.DataViewModel
import dk.itu.moapd.copenhagenbuzz.ceel.adapters.TimelineAdapter
import dk.itu.moapd.copenhagenbuzz.ceel.data.Event
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.FragmentTimelineBinding

/**
 * A simple [Fragment] subclass to display a list of events using [TimelineAdapter].
 */
class TimelineFragment : Fragment() {

    private var _binding: FragmentTimelineBinding? = null
    private val viewModel: DataViewModel by activityViewModels()
    private lateinit var adapter: TimelineAdapter

    private lateinit var timelineListView: ListView
    private lateinit var sortByButton: MaterialButton
    private val eventCategories = arrayOf("All", "Conference", "Workshop", "Concert", "Meetup", "Festival")

    private val binding get() = requireNotNull(_binding) {
        "Cannot access binding because it is null. Is the view visible?"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = FragmentTimelineBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timelineListView = binding.timelineListView
        sortByButton = binding.sortByButton!!

        sortByButton.text = "Sort by: All"

        // Set up click listener to show a PopupMenu with event categories.
        sortByButton.setOnClickListener {
            val popup = PopupMenu(requireContext(), sortByButton)

            eventCategories.forEach { category ->
                popup.menu.add(category)
            }

            popup.setOnMenuItemClickListener { menuItem ->
                val selectedCategory = menuItem.title.toString()

                // Update button text with the selected category.
                sortByButton.text = "Sort by: $selectedCategory"
                updateQuery(selectedCategory)
                true
            }
            popup.show()
        }

        //initialize with "All" to show all events at the beginning.
        updateQuery("All")
    }

    /**
     * Updates the Firebase query and adapter based on the selected event category.
     */
    private fun updateQuery(category: String) {
        val databaseRef = Firebase.database.getReference("copenhagen_buzz/events")

        val query = if (category == "All") {
            databaseRef.orderByChild("eventDate")
        } else {
            // Filter events where eventType equals the selected category.
            databaseRef.orderByChild("eventType").equalTo(category)
        }

        //build new FirebaseListOptions.
        val options = FirebaseListOptions.Builder<Event>()
            .setQuery(query, Event::class.java)
            .setLayout(R.layout.event_row_item)
            .setLifecycleOwner(this)
            .build()

        //if an adapter already exists, stop listening and clear it.
        //adapter.stopListening()

        //create a new adapter instance with the newly updated options.
        adapter = TimelineAdapter(options, viewModel)
        timelineListView.adapter = adapter

        // Start listening for data.
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}