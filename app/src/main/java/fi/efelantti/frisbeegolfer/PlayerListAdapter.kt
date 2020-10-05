package fi.efelantti.frisbeegolfer

import android.content.Context
import android.content.res.Resources
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

class PlayerListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<PlayerListAdapter.PlayerViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val res: Resources = context.resources
    private var players = emptyList<Player>() // Cached copy of words

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerItemViewFullName: TextView = itemView.findViewById(R.id.txtFullName)
        val playerItemViewEmail: TextView = itemView.findViewById(R.id.txtEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return PlayerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
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