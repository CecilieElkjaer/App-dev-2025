package dk.itu.moapd.copenhagenbuzz.ceel.helpers

import android.app.DatePickerDialog
import android.content.Context
import android.widget.EditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * A helper class to manage DatePicker interactions.
 */
class DatePickerHelper(private val context: Context) {

    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Attaches a DatePicker to an EditText field.
     *
     * @param editText The EditText field where the date will be displayed.
     */
    fun attachDatePicker(editText: EditText) {
        editText.setOnClickListener {
            val datePicker = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    editText.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }
}
