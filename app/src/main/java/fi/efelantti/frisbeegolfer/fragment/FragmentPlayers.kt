package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.LiveDataState
import fi.efelantti.frisbeegolfer.NewPlayerAction
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.adapter.PlayerListAdapter
import fi.efelantti.frisbeegolfer.databinding.FragmentPlayersBinding
import fi.efelantti.frisbeegolfer.model.Player
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModel
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModelFactory

class FragmentPlayers : SettingsMenuFragment(), PlayerListAdapter.ListItemClickListener,
    DialogConfirmDelete.OnConfirmationSelected, SearchView.OnQueryTextListener {

    private var _binding: FragmentPlayersBinding? = null
    private val binding get() = _binding!!
    private val playerViewModel: PlayerViewModel by activityViewModels {
        PlayerViewModelFactory((requireContext().applicationContext as FrisbeegolferApplication).repository)
    }
    private lateinit var adapter: PlayerListAdapter
    private var actionMode: ActionMode? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var fab: FloatingActionButton

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.appbar_actions, menu)
            mode.title = getString(R.string.player_selected)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_edit -> {
                    editSelectedPlayer()
                    mode.finish()
                    true
                }
                R.id.action_delete -> {
                    val player = adapter.getSelectedPlayer()
                        ?: throw java.lang.IllegalArgumentException("No player was selected.")
                    deletePlayer(player)
                    mode.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            adapter.resetSelectedPosition()
            fab.isEnabled = true
        }
    }

    private fun editSelectedPlayer() {
        val player = adapter.getSelectedPlayer()
        if (player != null) {
            val action =
                FragmentPlayersDirections.actionFragmentPlayersToFragmentNewPlayer(
                    NewPlayerAction.EDIT.toString(),
                    player.id
                )
            findNavController().navigate(action)
        }
        adapter.resetSelectedPosition()
    }

    private fun deletePlayer(player: Player) {
        DialogConfirmDelete(this, player, getString(R.string.player_type)).show(
            childFragmentManager, DialogScoreAmount.TAG
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayersBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val searchItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PlayerListAdapter(requireContext(), this)
        recyclerView = binding.recyclerview
        emptyView = binding.emptyView

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        playerViewModel.state.observe(viewLifecycleOwner, { state ->
            when (state) {
                LiveDataState.LOADING -> binding.progressBar.visibility = View.VISIBLE
                LiveDataState.SUCCESS -> binding.progressBar.visibility = View.GONE
            }
        })

        playerViewModel.allPlayers().observe(viewLifecycleOwner, { list ->
            list?.let { players ->
                if (players.count() == 0) {
                    binding.emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    binding.emptyView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    val sortedPlayers = players.sortedBy { it.name }
                    adapter.setPlayers(sortedPlayers)
                }
            }
        })

        fab = binding.fab
        fab.setOnClickListener {
            showNewPlayerDialog()
        }
    }

    private fun showNewPlayerDialog() {
        actionMode?.finish()
        val action =
            FragmentPlayersDirections.actionFragmentPlayersToFragmentNewPlayer(
                NewPlayerAction.ADD.toString(),
                -1L
            )
        findNavController().navigate(action)
    }

    override fun onListItemClick(position: Int, clickedOnSame: Boolean) {
        when (actionMode) {
            null -> {
                // Start the CAB using the ActionMode.Callback defined above
                editSelectedPlayer()
            }
            else -> {
                onListItemLongClick(position, clickedOnSame)
            }
        }
    }

    override fun onListItemLongClick(position: Int, clickedOnSame: Boolean) {
        if (clickedOnSame) {
            actionMode?.finish()
            binding.fab.isEnabled = true
        } else {
            binding.fab.isEnabled = false
            when (actionMode) {
                null -> {
                    // Start the CAB using the ActionMode.Callback defined above
                    actionMode = activity?.startActionMode(actionModeCallback)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
    }

    override fun returnUserConfirmation(playerToDelete: Any) {
        playerViewModel.delete(playerToDelete as Player)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        val resultsCount = adapter.filter(query)
        actOnFilterResults(resultsCount)
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val resultsCount = adapter.filter(newText)
        actOnFilterResults(resultsCount)
        return true
    }

    private fun actOnFilterResults(resultsCount: Int) {
        if (resultsCount == 0 && adapter.getAllItemsCount() > 0 && playerViewModel.state.value == LiveDataState.SUCCESS) {
            binding.recyclerview.visibility = View.GONE
            binding.noMatches.visibility = View.VISIBLE
        } else {
            binding.recyclerview.visibility = View.VISIBLE
            binding.noMatches.visibility = View.GONE
        }
    }
}