package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.efelantti.frisbeegolfer.*
import fi.efelantti.frisbeegolfer.databinding.FragmentPlayersBinding
import fi.efelantti.frisbeegolfer.model.Player
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModel
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModelFactory

class FragmentPlayers : Fragment(), PlayerListAdapter.ListItemClickListener {

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
        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.appbar_actions, menu)
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
            return when (item.itemId) {
                R.id.action_edit -> {
                    editSelectedCourse()
                    mode.finish() // Action picked, so close the CAB
                    true
                }
                else -> false
            }
        }

        private fun editSelectedCourse() {
            val player = adapter.getSelectedPlayer()
                ?: throw java.lang.IllegalArgumentException("No course was selected.")
            val fm: FragmentManager = parentFragmentManager
            val dialog: FragmentNewPlayer =
                FragmentNewPlayer.newInstance(NewPlayerAction.EDIT.toString(), player)
            dialog.show(fm, "fragment_newPlayer")
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            adapter.resetSelectedPosition()
        }
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

        playerViewModel.allPlayers.observe(viewLifecycleOwner, { players ->
            players?.let { adapter.setPlayers(it) }
        })

        fab = binding.fab
        fab.setOnClickListener {
            showNewPlayerDialog()
        }
    }

    // TODO - Navigation component
    private fun showNewPlayerDialog() {
        val fm: FragmentManager = parentFragmentManager
        val dialog: FragmentNewPlayer =
            FragmentNewPlayer.newInstance(
                NewPlayerAction.ADD.toString(),
                Player()
            )
        dialog.show(fm, "fragment_newPlayer")
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
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}