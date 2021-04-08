package fi.efelantti.frisbeegolfer.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.efelantti.frisbeegolfer.EmptyRecyclerView
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.PlayerListAdapterMultiSelect
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModel
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModelFactory


class FragmentChoosePlayers : Fragment(), PlayerListAdapterMultiSelect.ListItemClickListener {

    private val playerViewModel by viewModels<PlayerViewModel> {
        PlayerViewModelFactory((requireContext().applicationContext as FrisbeegolferApplication).repository)
    }
        interface FragmentChoosePlayersListener {

        fun onPlayersSelected(
            chosenPlayerIds: List<Long>
        )
    }

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

    private fun chooseSelectedPlayers() {
        val players = adapter.getSelectedPlayers()
        actionMode?.finish()
        if(players == null) throw java.lang.IllegalArgumentException("No players were selected.")
        sendBackResult(players.map{it.id})
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
        recyclerView = view.findViewById<EmptyRecyclerView>(
            R.id.recyclerview_choose_players
        )
        emptyView = view.findViewById<TextView>(R.id.empty_view_choose_players)
        recyclerView.setEmptyView(emptyView)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        playerViewModel.allPlayers.observe(viewLifecycleOwner, Observer { courses ->
            courses?.let { adapter.setPlayers(it) }
        })

        fab = view.findViewById<FloatingActionButton>(R.id.fab_choose_players)
        fab.setOnClickListener {
            chooseSelectedPlayers()
        }
   }

    // Call this method to send the data back to the parent activity
    private fun sendBackResult(chosenPlayersIds: List<Long>) {
        // Notice the use of `getTargetFragment` which will be set when the dialog is displayed
        val listener: FragmentChoosePlayersListener = activity as FragmentChoosePlayersListener
        listener.onPlayersSelected(chosenPlayersIds)
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
            val title = resources.getQuantityString(R.plurals.numberPlayersSelected, selectedPlayersCount, selectedPlayersCount)
            actionMode?.setTitle(title)
        }
    }
}