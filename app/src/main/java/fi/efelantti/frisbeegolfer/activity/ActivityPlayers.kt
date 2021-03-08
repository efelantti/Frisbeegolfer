package fi.efelantti.frisbeegolfer.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.efelantti.frisbeegolfer.*
import fi.efelantti.frisbeegolfer.fragment.FragmentNewPlayer
import fi.efelantti.frisbeegolfer.model.Player
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModel


class ActivityPlayers : AppCompatActivity(),
    FragmentNewPlayer.FragmentNewPlayerListener, PlayerListAdapter.ListItemClickListener {

    private val TAG = "ActivityPlayers"
    private val frisbeegolferViewModel: PlayerViewModel by viewModels()
    private lateinit var recyclerView: EmptyRecyclerView
    private lateinit var emptyView: TextView
    private var actionMode: ActionMode? = null
    private lateinit var adapter: PlayerListAdapter

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
                    editSelectedPlayer()
                    mode.finish() // Action picked, so close the CAB
                    true
                }
                else -> false
            }
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            adapter.resetSelectedPosition()
        }
    }

    private fun editSelectedPlayer() {
        val player = adapter.getSelectedPlayer()
        if(player == null) throw java.lang.IllegalArgumentException("No player was selected.")
        val fm: FragmentManager = supportFragmentManager
        val dialog: FragmentNewPlayer = FragmentNewPlayer.newInstance(NewPlayerAction.EDIT.toString(), player)
        dialog.show(fm, "fragment_newPlayer")
    }

    override fun onListItemClick(position: Int, shouldStartActionMode: Boolean) {
        if (!shouldStartActionMode) {
            actionMode?.finish()
        } else {
            when (actionMode) {
                null -> {
                    // Start the CAB using the ActionMode.Callback defined above
                    actionMode = startActionMode(actionModeCallback)
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_players)
        supportActionBar?.title = getString(R.string.players_activity_title)

        adapter = PlayerListAdapter(this, this)
        recyclerView = findViewById<EmptyRecyclerView>(
            R.id.recyclerview
        )
        emptyView = findViewById<TextView>(R.id.empty_view)

        recyclerView.setEmptyView(emptyView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        frisbeegolferViewModel.allPlayers.observe(this, Observer { players ->
            // Update the cached copy of the words in the adapter.
            players?.let { adapter.setPlayers(it) }
        })

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            showNewPlayerDialog()
        }
        }

    private fun showNewPlayerDialog() {
        val fm: FragmentManager = supportFragmentManager
        val dialog: FragmentNewPlayer =
            FragmentNewPlayer.newInstance(
                NewPlayerAction.ADD.toString(),
                Player()
            )
        dialog.show(fm, "fragment_newPlayer")
    }

    private fun checkIfPlayerAlreadyExists(player: Player, players: List<Player>?): Boolean {
        if (players == null) return false
        for(existingPlayer: Player in players) {
            if(Player.equals(
                    player,
                    existingPlayer
                )
            ){
                Log.e(TAG, "Could not add player data to database - duplicate.")
                val toast = Toast.makeText(this, HtmlCompat.fromHtml("<font color='" + getColor(R.color.colorErrorMessage) +"' ><b>" + getString(
                    R.string.error_duplicate_player
                ) + "</b></font>", HtmlCompat.FROM_HTML_MODE_LEGACY), Toast.LENGTH_LONG)
                val indexOfPlayer = players.indexOf(existingPlayer)
                recyclerView.scrollToPosition(indexOfPlayer)
                toast.show()
                return true
            }
        }
        return false
    }

    override fun onPlayerAdded(player: Player, result: Int) {
        if (result == Activity.RESULT_OK)
        {
            if(player == null) throw IllegalArgumentException("Player data was null.")
            else
            {
                val players = frisbeegolferViewModel.allPlayers.value
                var duplicateFound = checkIfPlayerAlreadyExists(player, players)
                if(!duplicateFound)
                {
                    frisbeegolferViewModel.insert(player)
                }
            }
        }
        else if(result == Activity.RESULT_CANCELED)
        {
            // Do nothing when canceled
        }
        else throw(IllegalArgumentException("Player data not returned from activity as expected."))
    }

    override fun onPlayerEdited(player: Player, result: Int) {
        if (result == Activity.RESULT_OK)
        {
            if(player == null) throw IllegalArgumentException("Player data was null.")
            else
            {
                val players = frisbeegolferViewModel.allPlayers.value
                var duplicateFound = checkIfPlayerAlreadyExists(player, players)
                if(!duplicateFound)
                {
                    frisbeegolferViewModel.update(player)
                }
            }
        }
        else if(result == Activity.RESULT_CANCELED)
        {
            // Do nothing when canceled
        }
        else throw(IllegalArgumentException("Player data not returned from activity as expected."))
    }
}