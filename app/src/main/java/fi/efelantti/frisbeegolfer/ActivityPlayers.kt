package fi.efelantti.frisbeegolfer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ActivityPlayers : AppCompatActivity() {

    private val frisbeegolferViewModel: PlayerViewModel by viewModels()
    private val newPlayerActivityRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_items)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = PlayerListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        frisbeegolferViewModel.allPlayers.observe(this, Observer { players ->
            // Update the cached copy of the words in the adapter.
            players?.let { adapter.setPlayers(it) }
        })

        //TODO - Add option to edit player data
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, NewPlayerActivity::class.java)
            startActivityForResult(intent, newPlayerActivityRequestCode)
        }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == newPlayerActivityRequestCode && resultCode == Activity.RESULT_OK) {
            val extras = data?.extras
            if (extras != null && data.hasExtra("firstName") && data.hasExtra("nickName") && data.hasExtra("lastName") && data.hasExtra("email")) {
                val firstName = extras.getString("firstName")
                val nickName: String? = extras.getString("nickName")
                val lastName: String? = extras.getString("lastName")
                val email: String? = extras.getString("email")

                val player = Player(firstName = firstName, nickName = nickName, lastName = lastName, email = email)
                frisbeegolferViewModel.insert(player)
            }
            else throw(IllegalArgumentException("Player data not returned from activity as expected."))
         }
    }
}