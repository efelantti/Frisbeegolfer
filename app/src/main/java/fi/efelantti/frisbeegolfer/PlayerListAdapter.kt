package fi.efelantti.frisbeegolfer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.selects.select


class PlayerListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<PlayerListAdapter.PlayerViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val res: Resources = context.resources
    private var players = emptyList<Player>() // Cached copy of words
    private val context = context
    private var selected_position = -1
    private var actionMode: ActionMode? = null

    private val actionModeCallback = object : ActionMode.Callback {
        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.appbar_actions, menu)
            mode.title = context.getString(R.string.player_selected)
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
            notifyDataSetChanged()
            selected_position = -1
        }
    }

    private fun editSelectedPlayer() {
        val player = players[selected_position]
        val intent = Intent(context, NewPlayerActivity::class.java)
        intent.putExtra("action", NewPlayerAction.EDIT.toString())
        intent.putExtra("playerData", player)
        (context as Activity).startActivityForResult(intent, NewPlayerAction.EDIT.id)
    }


    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerCard: CardView = itemView.findViewById(R.id.playerCard)
        val originalBackgroundColor: Int = playerCard.cardBackgroundColor.defaultColor
        val playerItemViewFullName: TextView = itemView.findViewById(R.id.txtFullName)
        val playerItemViewEmail: TextView = itemView.findViewById(R.id.txtEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return PlayerViewHolder(itemView)
    }

    // TODO - Change setBackgroundColor to Select?
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        if (selected_position == position) {
            holder.playerCard.setBackgroundColor(Color.YELLOW)

        } else {
            holder.playerCard.setBackgroundColor(holder.originalBackgroundColor)
        }

         holder.playerCard.setOnClickListener(View.OnClickListener {
            if (selected_position === position) {
                selected_position = -1
                notifyDataSetChanged()
                actionMode?.finish()
                return@OnClickListener
            }
            selected_position = position
            notifyDataSetChanged()

            when (actionMode) {
                null -> {
                    // Start the CAB using the ActionMode.Callback defined above
                    actionMode = (context as Activity).startActionMode(actionModeCallback)
                    true
                }
                else -> false
            }
        })

        val current = players[position]
        if(current.nickName.isNullOrBlank()) {
            holder.playerItemViewFullName.text = res.getString(R.string.fullname_without_nickname, current.firstName, current.lastName)
        } else
            holder.playerItemViewFullName.text = res.getString(R.string.fullname_with_nickname, current.firstName, current.nickName, current.lastName)
        var email = current.email?.trim()
        if(email.isNullOrBlank()) email = "-"
        holder.playerItemViewEmail.text = res.getString(R.string.email_descriptor, email)
    }

    internal fun setPlayers(players: List<Player>) {
        this.players = players
        notifyDataSetChanged()
    }

    override fun getItemCount() = players.size
}