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
import com.google.android.material.button.MaterialButton
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

        Log.d(TAG, "Populate an item at position: $position")
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

            //Changes the first letter of the eventType in the icon.
            eventTypeIcon.text = event.eventType.firstOrNull()?.toString() ?: "E"


            //Placeholders for liking an event, editing an event and clicking infor button.
            heartIcon.setOnClickListener {
                Log.d(TAG, "Heart icon clicked for event: ${event.eventName}")
            }

            editButton.setOnClickListener {
                Log.d(TAG, "Edit button clicked for event: ${event.eventName}")
            }

            infoButton.setOnClickListener {
                Log.d(TAG, "Info button clicked for event: ${event.eventName}")
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