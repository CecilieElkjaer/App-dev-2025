package dk.itu.moapd.copenhagenbuzz.ceel.adapters

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.ceel.R
import dk.itu.moapd.copenhagenbuzz.ceel.data.Event
import org.w3c.dom.Text
import java.time.format.DateTimeFormatter

class EventAdapter(private val context: Context, private val eventList: List<Event>) : BaseAdapter() {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun getCount(): Int {
        return eventList.size
    }

    override fun getItem(position: Int): Event {
        return eventList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.event_row_item, parent, false)
        val viewHolder = (view.tag as? ViewHolder) ?: ViewHolder(view)

        getItem(position)?.let { event ->
            populateViewHolder(viewHolder, event)
        }

        //Setting the viewHolder and returning the view object.
        view.tag = viewHolder
        return view
    }

    private fun populateViewHolder(viewHolder: ViewHolder, event: Event){
        with(viewHolder){
            eventTitle.text = event.eventName
            eventType.text = event.eventType
            eventLocation.text = event.eventLocation
            eventDate.text = event.eventDate.format(dateFormatter)
            eventDescription.text = event.eventDescription

            //setting a default image for each event, defined in the Event data class.
            eventImage.setImageResource(R.drawable.mockevent_img)

            //changes the first letter of the eventType in the icon.
            eventTypeIcon.text = event.eventType.firstOrNull()?.toString() ?: "E"

            //Placeholders for liking an event, editing an event and clicking info button.
            heartIcon.setOnClickListener { view ->
                Snackbar.make(view, "${event.eventName} was added to your favorites", Snackbar.LENGTH_SHORT)
                    .show()
            }

            editButton.setOnClickListener { view ->
                Snackbar.make(view, "Edit button clicked for event: ${event.eventName}", Snackbar.LENGTH_SHORT)
                    .show()
            }

            infoButton.setOnClickListener { view ->
                Snackbar.make(view, "Info button clicked for event: ${event.eventName}", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

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
    }
}