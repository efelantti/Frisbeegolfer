package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.efelantti.frisbeegolfer.*
import fi.efelantti.frisbeegolfer.databinding.FragmentPlayersBinding
import fi.efelantti.frisbeegolfer.model.Player
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModel
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModelFactory

class FragmentPlayers : SettingsMenuFragment(), PlayerListAdapter.ListItemClickListener,
    DialogConfirmDelete.OnConfirmationSelected {

    private var _binding: FragmentPlayersBinding? = null
    private val binding get() = _binding!!
    private val playerViewModel: PlayerViewModel by activityViewModels {
        PlayerViewModelFactory((requireContext().applicationContext as FrisbeegolferApplication).repository)
    }
    private lateinit var adapter: PlayerListAdapter
    private var actionMode: ActionMode? = null
    private lateinit var recyclerView: EmptyRecyclerView
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
            ?: throw java.lang.IllegalArgumentException("No player was selected.")
        val action =
            FragmentPlayersDirections.actionFragmentPlayersToFragmentNewPlayer(
                NewPlayerAction.EDIT.toString(),
                player.id
            )
        adapter.resetSelectedPosition()
        findNavController().navigate(action)
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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PlayerListAdapter(requireContext(), this)
        recyclerView = binding.recyclerview
        emptyView = binding.emptyView
        recyclerView.setEmptyView(emptyView)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        playerViewModel.allPlayers.observe(viewLifecycleOwner, { list ->
            list?.let { players ->
                val sortedPlayers = players.sortedBy { it.name }
                adapter.setPlayers(sortedPlayers)
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
}