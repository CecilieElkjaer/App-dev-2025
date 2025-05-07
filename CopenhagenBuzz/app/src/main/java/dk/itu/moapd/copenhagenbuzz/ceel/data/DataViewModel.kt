package dk.itu.moapd.copenhagenbuzz.ceel.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.javafaker.Faker
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class DataViewModel : ViewModel() {

    //LiveData which holds the list of events from the Firebase Database
    private val _events = MutableLiveData<List<Event>>()

    //LiveData which holds the list of favorites for the current user
    private val _favorites = MutableLiveData<List<Event>>()

    //The Firebase database instance
    private val database = Firebase.database

    //listeners to subscribe/unsubscribe from Firebase updates
    private var eventsListener: ValueEventListener? = null
    private var favoritesListener: ValueEventListener? = null

    init {
        eventsListener()
        favoritesListener()
    }

    /**
    Listens for any changes for the "copenhagen_buzz/events" node in the database, and then updates accordingly.
     */
    private fun eventsListener() {
        val eventsRef = database.getReference("copenhagen_buzz/events")

        eventsListener = eventsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //a list that holds the events fetched from the Firebase database
                val eventsList = mutableListOf<Event>()
                for (child in snapshot.children) {
                    //convert each child snapshot into an Event object
                    child.getValue(Event::class.java)?.let { eventsList.add(it) }
                }
                //posting the list of events to the LiveData so observers can update
                _events.postValue(eventsList)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
    Listens for any changes to the current user's favorites in the database on the "copenhagen_buzz/favorites/<currentUserId>" node,
    and then updates the LiveData with the current list of favorite events for the current user.
     */
    private fun favoritesListener() {
        // Get the current user's ID from FirebaseAuth; if not available, do nothing
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favRef = database.getReference("copenhagen_buzz/favorites").child(currentUserId)

        favoritesListener = favRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //a list that holds the favorite events fetched from Firebase database
                val favList = mutableListOf<Event>()
                for (child in snapshot.children) {
                    //convert each child snapshot into an Event object, and adding the event to list of favorite events.
                    child.getValue(Event::class.java)?.let { favList.add(it) }
                }
                //posting the favorite events list to the LiveData
                _favorites.postValue(favList)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Called when the ViewModel is about to be destroyed.
     * Removes Firebase listeners to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        eventsListener?.let {
            database.getReference("copenhagen_buzz/events").removeEventListener(it)
        }
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null && favoritesListener != null) {
            database.getReference("copenhagen_buzz/favorites").child(currentUserId)
                .removeEventListener(favoritesListener!!)
        }
    }


    // Adds the event to the local favorites list immediately
    private fun addFavoriteLocally(event: Event) {
        val currentFav = _favorites.value?.toMutableList() ?: mutableListOf()

        if (!currentFav.contains(event)) {
            currentFav.add(event)
            _favorites.postValue(currentFav)
        }
    }

    // Removes the event from the local favorites list immediately
    private fun removeFavoriteLocally(event: Event) {
        val currentFav = _favorites.value?.toMutableList() ?: mutableListOf()

        if (currentFav.contains(event)) {
            currentFav.remove(event)
            _favorites.postValue(currentFav)
        }
    }

    /**
     * Toggle the favorite status for the given event.
     * @param event The event object.
     * @param eventKey The unique key of the event in Firebase.
     */
    fun toggleFavoriteButton(event: Event, eventKey: String?) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (eventKey == null) return

        if (!isFavorite(event)) {
            //add the event locally
            addFavoriteLocally(event)

            //update the database to add the event as favorite
            database.getReference("copenhagen_buzz/favorites")
                .child(currentUserId)
                .child(eventKey)
                .setValue(event)
        } else {
            //remove the event locally
            removeFavoriteLocally(event)

            //update the database to remove the event from favorites
            database.getReference("copenhagen_buzz/favorites")
                .child(currentUserId)
                .child(eventKey)
                .removeValue()
        }
    }

    fun getEvents(): LiveData<List<Event>> = _events
    /**
     * Checks if the given event is marked as favorite locally.
     * (This may need to be updated to also listen to the Firebase favorites node.)
     */
    fun isFavorite(event: Event): Boolean {
        return _favorites.value?.contains(event) ?: false
    }


}