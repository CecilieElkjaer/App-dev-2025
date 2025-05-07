package dk.itu.moapd.copenhagenbuzz.ceel.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import dk.itu.moapd.copenhagenbuzz.ceel.ui.components.CalendarScreen

class CalendarFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CalendarScreen() // Call your calendar composable here
            }
        }
    }
}