package dk.itu.moapd.copenhagenbuzz.ceel.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import dk.itu.moapd.copenhagenbuzz.ceel.R
import dk.itu.moapd.copenhagenbuzz.ceel.data.Event

class FavoritesAdapter(options: FirebaseRecyclerOptions<Event>) : FirebaseRecyclerAdapter<Event, FavoritesAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.favorites_row_item, parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int, model: Event) {
        viewHolder.bind(model)
    }

    /**
     * Inner ViewHolder for FavoritesAdapter.
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventName : TextView = view.findViewById(R.id.event_title)
        val eventType : TextView = view.findViewById(R.id.event_type)
        val eventImage : ImageView = view.findViewById(R.id.event_image_view)
        val eventTypeIcon : TextView = view.findViewById(R.id.event_type_icon)

        /**
         * Binds the favorite event data to the UI components.
         */
        fun bind(event: Event) {
            eventName.text = event.eventName
            eventType.text = event.eventType

            //displaying the first letter of the event type or a default "E"
            eventTypeIcon.text = event.eventType.firstOrNull()?.toString() ?: "E"

            //setting a default image resource (should be changed)
            eventImage.setImageResource(R.drawable.mockevent_img)
        }
    }
}