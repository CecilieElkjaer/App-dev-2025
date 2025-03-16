package dk.itu.moapd.copenhagenbuzz.ceel.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dk.itu.moapd.copenhagenbuzz.ceel.R
import dk.itu.moapd.copenhagenbuzz.ceel.adapters.FavoritesAdapter
import dk.itu.moapd.copenhagenbuzz.ceel.data.DataViewModel
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.FragmentCalendarBinding
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.FragmentFavoritesBinding

/**
 * A simple [Fragment] subclass.
 * Use the [FavoritesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val viewModel: DataViewModel by activityViewModels()
    private lateinit var adapter: FavoritesAdapter

    private val binding get() = requireNotNull(_binding) {
        "Cannot access binding because it is null. Is the view visible?"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentFavoritesBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FavoritesAdapter(emptyList())
        binding.favoriteRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.favoriteRecyclerView.adapter = adapter

        viewModel.favorites.observe(viewLifecycleOwner) { events ->
            adapter.updateFavorites(events)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}