package fi.efelantti.frisbeegolfer

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import fi.efelantti.frisbeegolfer.model.Player


class PlayerListAdapterMultiSelect internal constructor(
    context: Context,
    onClickListener: ListItemClickListener
) : RecyclerView.Adapter<PlayerListAdapterMultiSelect.PlayerViewHolder>() {

    interface ListItemClickListener {
        fun onListItemClick(position: Int, shouldStartActionMode: Boolean)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val res: Resources = context.resources
    private var players = emptyList<Player>() // Cached copy of words
    var selectedIndeces = mutableListOf<Int>()
    private val mOnClickListener: ListItemClickListener = onClickListener

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val playerCard: CardView = itemView.findViewById(R.id.playerCard)
        val originalBackgroundColor: Int = playerCard.cardBackgroundColor.defaultColor
        val playerItemViewName: TextView = itemView.findViewById(R.id.txtFullName)
        val playerItemViewEmail: TextView = itemView.findViewById(R.id.txtEmail)

        init{
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = bindingAdapterPosition
            players[position]
            val shouldStartActionMode: Boolean
            (if (selectedIndeces.contains(position)) {
                selectedIndeces.remove(position)
                notifyItemChanged(position)
                selectedIndeces.count() != 0
            } else {
                selectedIndeces.add(position)
                notifyItemChanged(position)
                true
            }).also { shouldStartActionMode = it }
            mOnClickListener.onListItemClick(position, shouldStartActionMode)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_player, parent, false)
        return PlayerViewHolder(itemView)
    }

    // TODO - Change setBackgroundColor to Select?
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val selectedPlayer = players[position]
        if (selectedIndeces.contains(position)) {
            holder.playerCard.setBackgroundColor(Color.YELLOW)

        } else {
            holder.playerCard.setBackgroundColor(holder.originalBackgroundColor)
        }
        holder.playerItemViewName.text = selectedPlayer.name
        var email = selectedPlayer.email?.trim()
        if(email.isNullOrBlank()) email = "-"
        holder.playerItemViewEmail.text = res.getString(R.string.email_descriptor, email)
    }

    internal fun getSelectedPlayers(): List<Player> {
        return selectedIndeces.map{players[it]}
    }

    internal fun setPlayers(players: List<Player>) {
        this.players = players
        notifyDataSetChanged()
    }

    internal fun resetSelectedPlayers() {
        val previousSelectedIndeces = selectedIndeces
        selectedIndeces = mutableListOf()
        for (index in previousSelectedIndeces) {
            notifyItemChanged(index)
        }
    }

    override fun getItemCount() = players.size
}