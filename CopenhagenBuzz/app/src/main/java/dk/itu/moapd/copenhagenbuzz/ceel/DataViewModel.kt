package dk.itu.moapd.copenhagenbuzz.ceel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.javafaker.Faker
import dk.itu.moapd.copenhagenbuzz.ceel.data.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random

class DataViewModel : ViewModel() {
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events // Exposed LiveData
    private val faker = Faker()

    init {
        fetchEvents() // Fetch mock data when ViewModel initializes
    }

    //Fetch event data async by using coroutines
    private fun fetchEvents(){
        viewModelScope.launch(Dispatchers.IO) {
            delay(1000)
            val mockEvents = generateMockEvents(10) //generates 10 mock events
            _events.postValue(mockEvents)
        }
    }

    private fun generateMockEvents(count: Int): List<Event> {
        val eventList = mutableListOf<Event>()
        for (i in 1..count) {
            val photoUrl = "https://source.unsplash.com/random/800x600?sig=$i"
            val name = faker.book().title()
            val location = faker.address().city()
            val date = LocalDate.now().plusDays(Random.nextLong(1, 30))
            val type = faker.job().field()
            val description = faker.lorem().sentence(10)

            eventList.add(Event(photoUrl, name, location, date, type, description))
        }
        return eventList
    }

}