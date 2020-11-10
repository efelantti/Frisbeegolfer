package fi.efelantti.frisbeegolfer.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.efelantti.frisbeegolfer.*
import fi.efelantti.frisbeegolfer.fragment.FragmentNewPlayer
import fi.efelantti.frisbeegolfer.model.Player
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModel


class ActivityPlayers : AppCompatActivity(),
    FragmentNewPlayer.FragmentNewPlayerListener {

    private val TAG = "ActivityPlayers"
    private val frisbeegolferViewModel: PlayerViewModel by viewModels()

    private lateinit var recyclerView: EmptyRecyclerView
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_players)
        supportActionBar?.title = getString(R.string.players_activity_title)

        var adapter = PlayerListAdapter(this)
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
                val toast = Toast.makeText(this, HtmlCompat.fromHtml("<font color='#FF0000' ><b>" + getString(
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