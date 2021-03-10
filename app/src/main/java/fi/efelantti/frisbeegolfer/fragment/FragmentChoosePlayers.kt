package fi.efelantti.frisbeegolfer.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fi.efelantti.frisbeegolfer.*
import fi.efelantti.frisbeegolfer.model.clone
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModel

class FragmentChoosePlayers : Fragment(), PlayerListAdapterMultiSelect.ListItemClickListener {

    private val courseViewModel: PlayerViewModel by viewModels()

    interface FragmentChoosePlayersListener {

        fun onPlayersSelected(
            chosenPlayerIds: List<Long>
        )
    }

    private lateinit var adapter: PlayerListAdapterMultiSelect
    private var actionMode: ActionMode? = null
    private lateinit var recyclerView: EmptyRecyclerView

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
                    chooseSelectedPlayers()
                    mode.finish() // Action picked, so close the CAB
                    true
                }
                else -> false
            }
        }

        private fun chooseSelectedPlayers() {
            val players = adapter.getSelectedPlayers()
            if(players == null) throw java.lang.IllegalArgumentException("No players were selected.")
            sendBackResult(players.map{it.id})
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
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

        /* TODO - Toolbar
        val toolbar: Toolbar = view.findViewById(R.id.dialog_toolbar_new_course)
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.inflateMenu(R.menu.appbar_dialog)
         */

        adapter = PlayerListAdapterMultiSelect(activity as Context, this)
        recyclerView = view.findViewById<EmptyRecyclerView>(
            R.id.recyclerview_choose_players
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        courseViewModel.allPlayers.observe(viewLifecycleOwner, Observer { courses ->
            courses?.let { adapter.setPlayers(it) }
        })
   }

    // Call this method to send the data back to the parent activity
    fun sendBackResult(chosenPlayersIds: List<Long>) {
        // Notice the use of `getTargetFragment` which will be set when the dialog is displayed
        val listener: FragmentChoosePlayersListener = activity as FragmentChoosePlayersListener
        listener.onPlayersSelected(chosenPlayersIds)
    }

    override fun onListItemClick(position: Int, shouldStartActionMode: Boolean) {
        if (!shouldStartActionMode) {
            actionMode?.finish()
        } else {
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
}