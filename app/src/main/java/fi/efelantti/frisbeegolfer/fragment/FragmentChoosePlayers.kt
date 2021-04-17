package fi.efelantti.frisbeegolfer.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.efelantti.frisbeegolfer.EmptyRecyclerView
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.PlayerListAdapterMultiSelect
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModel
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModelFactory
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModelFactory
import java.time.OffsetDateTime


class FragmentChoosePlayers : Fragment(), PlayerListAdapterMultiSelect.ListItemClickListener {

    private val playerViewModel: PlayerViewModel by activityViewModels {
        PlayerViewModelFactory((requireContext().applicationContext as FrisbeegolferApplication).repository)
    }
    private val roundViewModel: RoundViewModel by viewModels {
        RoundViewModelFactory((requireContext().applicationContext as FrisbeegolferApplication).repository)
    }
    private val args: FragmentChoosePlayersArgs by navArgs()
    private lateinit var adapter: PlayerListAdapterMultiSelect
    private var actionMode: ActionMode? = null
    private lateinit var recyclerView: EmptyRecyclerView
    private lateinit var emptyView: TextView
    private lateinit var fab: FloatingActionButton

    private val actionModeCallback = object : ActionMode.Callback {
        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.appbar_choose_course_or_players, menu)
            mode.title = getString(R.string.player_selected)
            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return false
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            fab.isEnabled = false
            adapter.resetSelectedPlayers()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_choose_players, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PlayerListAdapterMultiSelect(activity as Context, this)
        recyclerView = view.findViewById(
            R.id.recyclerview_choose_players
        )
        emptyView = view.findViewById(R.id.empty_view_choose_players)
        recyclerView.setEmptyView(emptyView)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        playerViewModel.allPlayers.observe(viewLifecycleOwner, { courses ->
            courses?.let { adapter.setPlayers(it) }
        })

        fab = view.findViewById(R.id.fab_choose_players)
        fab.setOnClickListener {
            chooseSelectedPlayers()
        }
    }

    override fun onListItemClick(position: Int, shouldStartActionMode: Boolean) {
        if (!shouldStartActionMode) {
            actionMode?.finish()
            fab.isEnabled = false
        } else {
            fab.isEnabled = true
            when (actionMode) {
                null -> {
                    // Start the CAB using the ActionMode.Callback defined above
                    actionMode = activity?.startActionMode(actionModeCallback)
                    true
                }
                else -> false
            }
            val selectedPlayersCount = adapter.selectedIndeces.count()
            val title = resources.getQuantityString(
                R.plurals.numberPlayersSelected,
                selectedPlayersCount,
                selectedPlayersCount
            )
            actionMode?.title = title
        }
    }

    private fun chooseSelectedPlayers() {
        val players = adapter.getSelectedPlayers()
        actionMode?.finish()
        val courseId = args.courseId
        val roundId = OffsetDateTime.now()
        roundViewModel.addRoundToDatabase(courseId, players.map { it.id }, roundId)
        navigateToScoreFragment(roundId)
    }

    private fun navigateToScoreFragment(roundId: OffsetDateTime) {
        val action =
            FragmentChoosePlayersDirections.actionFragmentChoosePlayersToFragmentScore(roundId)
        findNavController().navigate(action)
    }
}