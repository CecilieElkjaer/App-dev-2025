package dk.itu.moapd.copenhagenbuzz.ceel.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import dk.itu.moapd.copenhagenbuzz.ceel.R
import dk.itu.moapd.copenhagenbuzz.ceel.adapters.FavoritesAdapter
import dk.itu.moapd.copenhagenbuzz.ceel.data.DataViewModel
import dk.itu.moapd.copenhagenbuzz.ceel.data.Event
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = FragmentFavoritesBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Getting the userId of the current user
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        //building the Firebase query for the favorites of this user.
        val query = Firebase.database.getReference("copenhagen_buzz/favorites")
            .child(currentUserId)
            .orderByChild("eventDate")

        //configuring the FirebaseRecyclerOptions
        val options = FirebaseRecyclerOptions.Builder<Event>()
            .setQuery(query, Event::class.java)
            .build()

        //Initializing the adapter by using the Firebase options.
        adapter = FavoritesAdapter(options)

        binding.favoriteRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.favoriteRecyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        //the adapter should start listening for changes in the database.
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        //the adapter should stop listening for changes in the database when the fragment is not visible.
        adapter.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}