package dk.itu.moapd.copenhagenbuzz.ceel.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    val datePickerState = rememberDatePickerState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Select a date", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        DatePicker(state = datePickerState)

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            selectedDate = datePickerState.selectedDateMillis
        }) {
            Text("Confirm Date")
        }

        Spacer(modifier = Modifier.height(16.dp))
        selectedDate?.let {
            Text("Selected: ${convertMillisToDate(it)}")
        }
    }
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}
