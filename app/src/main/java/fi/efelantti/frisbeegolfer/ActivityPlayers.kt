package fi.efelantti.frisbeegolfer

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class ActivityPlayers : AppCompatActivity() {

    private val TAG = "ActivityPlayers"
    private val frisbeegolferViewModel: PlayerViewModel by viewModels()

    private lateinit var recyclerView: EmptyRecyclerView
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_items)
        supportActionBar?.title = getString(R.string.players_activity_title)

        var adapter = PlayerListAdapter(this)
        recyclerView = findViewById<EmptyRecyclerView>(R.id.recyclerview)
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
            val intent = Intent(this, NewPlayerActivity::class.java)
            intent.putExtra("action", NewPlayerAction.ADD.toString())
            startActivityForResult(intent, NewPlayerAction.ADD.id)
        }
        }

    // TODO - Highlight the already existing item.
    // TODO - Refactor.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if ((requestCode == NewPlayerAction.ADD.id || requestCode == NewPlayerAction.EDIT.id) && resultCode == Activity.RESULT_OK) {
            val extras = data?.extras
            if (extras != null) {
                val player = extras.getParcelable<Player>("playerData")

                if(player == null) throw IllegalArgumentException("Player data was null.")
                val players = frisbeegolferViewModel.allPlayers.value
                var duplicateFound: Boolean = false
                if (players != null) {
                    for(existingPlayer: Player in players) {
                        if(Player.equals(player, existingPlayer)){
                            Log.e(TAG, "Could not add player data to database - duplicate.")
                            val toast = Toast.makeText(this, HtmlCompat.fromHtml("<font color='#FF0000' ><b>" + getString(R.string.error_duplicate_player) + "</b></font>", HtmlCompat.FROM_HTML_MODE_LEGACY), Toast.LENGTH_LONG)
                            val indexOfPlayer = players.indexOf(existingPlayer)
                            recyclerView.scrollToPosition(indexOfPlayer)
                            toast.show()
                            duplicateFound = true
                            break
                    }
                }
                    if(!duplicateFound)
                    {
                        when(requestCode)
                        {
                            NewPlayerAction.ADD.id -> frisbeegolferViewModel.insert(player)
                            NewPlayerAction.EDIT.id -> frisbeegolferViewModel.update(player)
                        }

                    }
                    }
                }
            }
            else if(resultCode == Activity.RESULT_CANCELED)
            {
                // Do nothing when canceled
            }
            else throw(IllegalArgumentException("Player data not returned from activity as expected."))
         }
    }