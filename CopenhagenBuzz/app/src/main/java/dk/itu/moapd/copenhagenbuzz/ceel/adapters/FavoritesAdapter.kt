package dk.itu.moapd.copenhagenbuzz.ceel.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dk.itu.moapd.copenhagenbuzz.ceel.R
import dk.itu.moapd.copenhagenbuzz.ceel.data.Event

class FavoritesAdapter(private var favoriteEvents: List<Event>) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.favorites_row_item, parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val event = favoriteEvents[position]
        viewHolder.eventName.text = event.eventName
        viewHolder.eventType.text = event.eventType

        //changes the first letter of the eventType in the icon.
        viewHolder.eventTypeIcon.text = event.eventType.firstOrNull()?.toString() ?: "E"

        viewHolder.eventImage.setImageResource(R.drawable.mockevent_img)
    }

    override fun getItemCount(): Int {
        return favoriteEvents.size
    }

    fun updateFavorites(newFavorites : List<Event>) {
        favoriteEvents = newFavorites
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventName : TextView = view.findViewById(R.id.event_title)
        val eventType : TextView = view.findViewById(R.id.event_type)
        val eventImage : ImageView = view.findViewById(R.id.event_image_view)
        val eventTypeIcon : TextView = view.findViewById(R.id.event_type_icon)
    }
}