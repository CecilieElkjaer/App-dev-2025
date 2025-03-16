package dk.itu.moapd.copenhagenbuzz.ceel.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.javafaker.Faker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random

class DataViewModel : ViewModel() {
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events // Exposed LiveData

    private val _favorite = MutableLiveData<List<Event>>()
    val favorites: LiveData<List<Event>> get() = _favorite //exposed the list of favorite events

    private val faker = Faker()

    init {
        fetchEvents() // Fetch mock data when ViewModel initializes
    }

    //Fetch event data async by using coroutines
    private fun fetchEvents(){
        viewModelScope.launch(Dispatchers.IO) {
            delay(10)
            val mockEvents = generateMockEvents(10) //generates 10 mock events
            _events.postValue(mockEvents)

            _favorite.postValue(generateRandomFavorites(mockEvents))
        }
    }

    private fun generateRandomFavorites(events: List <Event >): List <Event > {
        val shuffledIndices = (events.indices).shuffled().take(5).sorted()
        return shuffledIndices.mapNotNull { index -> events.getOrNull(index) }
    }

    private fun generateMockEvents(count: Int): List<Event> {
        val eventList = mutableListOf<Event>()
        for (i in 1..count) {
            val photoUrl = "https://unsplash.com/photos/aerial-view-of-a-rocky-beach-and-ocean-dDwRatblC1Y"
            val name = faker.book().title()
            val location = faker.address().city()
            val date = LocalDate.now().plusDays(Random.nextLong(1, 30))
            val type = faker.job().field()
            val description = faker.lorem().sentence(10)

            eventList.add(Event(photoUrl, name, location, date, type, description))
        }
        return eventList
    }

    fun addEvent(newEvent: Event) {
        val currentList = _events.value?.toMutableList() ?: mutableListOf()
        currentList.add(0, newEvent)
        _events.value = currentList // Update LiveData
    }

    fun toggleFavoriteButton(event: Event) {
        val currentFavorites = _favorite.value?.toMutableList() ?: mutableListOf()
        if (currentFavorites.contains(event)) {
            currentFavorites.remove(event) // Remove if already favorite
        } else {
            currentFavorites.add(event) // Add if not favorite
        }
        _favorite.value = currentFavorites
    }

    // Check if an event is in favorites
    fun isFavorite(event: Event): Boolean {
        return _favorite.value?.contains(event) ?: false
    }
}