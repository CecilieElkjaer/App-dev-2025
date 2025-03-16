package dk.itu.moapd.copenhagenbuzz.ceel.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import dk.itu.moapd.copenhagenbuzz.ceel.data.DataViewModel
import dk.itu.moapd.copenhagenbuzz.ceel.adapters.EventAdapter
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.FragmentTimelineBinding

/**
 * A simple [Fragment] subclass to display a list of events using [EventAdapter].
 */
class TimelineFragment : Fragment() {

    private var _binding: FragmentTimelineBinding? = null
    private val viewModel: DataViewModel by activityViewModels()
    private lateinit var adapter: EventAdapter

    private val binding get() = requireNotNull(_binding) {
        "Cannot access binding because it is null. Is the view visible?"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentTimelineBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EventAdapter(requireContext(), emptyList(),viewModel)
        binding.timelineListView.adapter = adapter

        //below makes sure that changes in the event list is observed
        viewModel.events.observe(viewLifecycleOwner) { events ->
            val adapter = EventAdapter(requireContext(), events, viewModel)
            binding.timelineListView.adapter = adapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}