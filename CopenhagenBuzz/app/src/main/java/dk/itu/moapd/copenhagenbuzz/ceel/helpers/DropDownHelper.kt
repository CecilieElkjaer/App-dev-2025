package dk.itu.moapd.copenhagenbuzz.ceel.helpers

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView

/**
 * A helper class for managing dropdown menus.
 */
class DropDownHelper(private val context: Context) {

    private val eventTypes = arrayOf("Conference", "Workshop", "Concert", "Meetup", "Festival")

    /**
     * Populates the event type dropdown menu.
     *
     * @param dropdownView The AutoCompleteTextView where the dropdown will be applied.
     */
    fun setupEventTypeDropdown(dropdownView: AutoCompleteTextView) {
        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, eventTypes)
        dropdownView.setAdapter(adapter)
    }
}