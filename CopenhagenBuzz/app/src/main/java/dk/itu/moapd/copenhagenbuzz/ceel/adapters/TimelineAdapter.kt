package dk.itu.moapd.copenhagenbuzz.ceel.adapters

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.copenhagenbuzz.ceel.R
import dk.itu.moapd.copenhagenbuzz.ceel.data.DataViewModel
import dk.itu.moapd.copenhagenbuzz.ceel.data.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimelineAdapter(options: FirebaseListOptions<Event>, private val viewModel: DataViewModel) : FirebaseListAdapter<Event>(options) {

    override fun populateView(view: View, event: Event, position: Int) {
        //getting the unique key for this event from the database
        val eventKey = getRef(position).key

        //retrieve the ViewHolder (or create one if not available in the view tag)
        val viewHolder = (view.tag as? ViewHolder) ?: ViewHolder(view)
        viewHolder.bind(event, eventKey)    }

    /**
     * Updates the favorite icon, when an event is liked.
     */
    private fun stateOfFavoriteIcon(heartIcon: ImageView, event: Event) {
        val isFavorite = viewModel.isFavorite(event)
        heartIcon.setImageResource(
            if (isFavorite) R.drawable.baseline_favorite_like_24
            else R.drawable.baseline_favorite_border_24
        )
    }

    /**
     * Inner ViewHolder that holds references to the views in each row.
     */
    private inner class ViewHolder(view: View){
        val eventTitle: TextView = view.findViewById(R.id.event_title)
        val eventType: TextView = view.findViewById(R.id.event_type)
        val eventLocation: TextView = view.findViewById(R.id.event_location)
        val eventDate: TextView = view.findViewById(R.id.event_date)
        val eventDescription: TextView = view.findViewById(R.id.event_description)
        val eventImage: ImageView = view.findViewById(R.id.event_image_view)
        val eventTypeIcon: TextView = view.findViewById(R.id.eventType_icon)
        val heartIcon: ImageView = view.findViewById(R.id.heart_icon)
        val editButton: MaterialButton = view.findViewById(R.id.button_edit_event)
        val infoButton: MaterialButton = view.findViewById(R.id.button_info_on_event)
        private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        /**
         * Binds the event data to the UI components.
         * @param event The event data.
         * @param eventKey The unique key from Firebase.
         */
        fun bind (event: Event, eventKey: String?){
            eventTitle.text = event.eventName
            eventType.text = event.eventType
            eventLocation.text = event.eventLocation
            eventDate.text = dateFormatter.format(Date(event.eventDate))
            eventDescription.text = event.eventDescription
            eventImage.setImageResource(R.drawable.mockevent_img)
            eventTypeIcon.text = event.eventType.firstOrNull()?.toString() ?: "E"

            stateOfFavoriteIcon(heartIcon, event)

            heartIcon.setOnClickListener { view ->
                if (eventKey != null) {
                    //check if the event is liked by the user
                    if (!viewModel.isFavorite(event)) {
                        //change icon and show added snackbar
                        heartIcon.setImageResource(R.drawable.baseline_favorite_like_24)
                        Snackbar.make(view, "${event.eventName} added to favorites", Snackbar.LENGTH_SHORT)
                            .show()
                    } else {
                        //change icon and show removed snackbar
                        heartIcon.setImageResource(R.drawable.baseline_favorite_border_24)
                        Snackbar.make(view, "${event.eventName} removed from favorites", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                    // Use the toggle which updates both local state and Firebase
                    viewModel.toggleFavoriteButton(event, eventKey)
                }
            }

            // Show the edit button only if the current user is the creator.
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId != null && currentUserId == event.userId) {
                editButton.visibility = View.VISIBLE
                editButton.setOnClickListener { view ->
                    //navigate to the EditEventFragment with the event key
                    val bundle = Bundle().apply {
                        putString("eventKey", eventKey)
                    }
                    view.findNavController().navigate(
                        R.id.action_timeline_to_edit_event,
                        bundle
                    )
                }
            } else {
                editButton.visibility = View.GONE
            }

            infoButton.setOnClickListener { view ->
                Snackbar.make(view, "Info button clicked for event: ${event.eventName}", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }
}