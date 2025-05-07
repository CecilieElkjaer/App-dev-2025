package dk.itu.moapd.copenhagenbuzz.ceel.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.ceel.data.DataViewModel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val datePickerState = rememberDatePickerState()
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var eventName by remember { mutableStateOf<String?>(null) }

    // Automatically update selectedDate and eventName when user picks a date
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let {
            selectedDate = it
            eventName = getEventForDate(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Select a date", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        DatePicker(state = datePickerState)
        Spacer(modifier = Modifier.height(16.dp))

        selectedDate?.let {
            Text(
                text = "Selected: ${convertMillisToDate(it)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant

            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        eventName?.let {
            Text(
                text = "Event: $it",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper function to convert milliseconds to Date
fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(millis))
}

suspend fun getEventForDate(dateMillis: Long): String? = suspendCancellableCoroutine { continuation ->
    val databaseRef = Firebase.database.getReference("copenhagen_buzz/events")

    databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
        val dateMillisConverted = convertMillisToDate(dateMillis)
        override fun onDataChange(snapshot: DataSnapshot) {
            for (eventSnapshot in snapshot.children) {
                val eventDate = eventSnapshot.child("eventDate").getValue(Long::class.java)
                val eventDateConverted = convertMillisToDate(eventDate!!)
                val eventName = eventSnapshot.child("eventName").getValue(String::class.java)
                val eventType = eventSnapshot.child("eventType").getValue(String::class.java)

                if (eventDateConverted == dateMillisConverted) {
                    val eventInfo = "$eventName ($eventType)"
                    continuation.resume(eventInfo) // Return the event name
                    return
                }
            }
            continuation.resume(null) // No matching event
        }

        override fun onCancelled(error: DatabaseError) {
            continuation.resume(null)
        }
    })
}
