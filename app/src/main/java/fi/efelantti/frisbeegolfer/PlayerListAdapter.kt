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


class PlayerListAdapter internal constructor(
    context: Context,
    onClickListener: PlayerListAdapter.ListItemClickListener
) : RecyclerView.Adapter<PlayerListAdapter.PlayerViewHolder>() {

    interface ListItemClickListener {
        fun onListItemClick(position: Int, shouldStartActionMode: Boolean)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val res: Resources = context.resources
    private var players = emptyList<Player>() // Cached copy of words
    private var defaultSelectedPosition = -1
    var selectedPosition = defaultSelectedPosition
    private val mOnClickListener: PlayerListAdapter.ListItemClickListener = onClickListener

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val playerCard: CardView = itemView.findViewById(R.id.playerCard)
        val originalBackgroundColor: Int = playerCard.cardBackgroundColor.defaultColor
        val playerItemViewName: TextView = itemView.findViewById(R.id.txtFullName)
        val playerItemViewEmail: TextView = itemView.findViewById(R.id.txtEmail)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = getAdapterPosition()
            var previousSelectedPosition = selectedPosition
            var shouldStartActionMode: Boolean
            if (selectedPosition == position) {
                resetSelectedPosition()
                shouldStartActionMode = false
            } else {
                selectedPosition = position
                notifyItemChanged(previousSelectedPosition)
                notifyItemChanged(selectedPosition)
                shouldStartActionMode = true
            }
            mOnClickListener.onListItemClick(position, shouldStartActionMode)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_player, parent, false)
        return PlayerViewHolder(itemView)
    }

    // TODO - Change setBackgroundColor to Select?
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        if (selectedPosition == position) {
            holder.playerCard.setBackgroundColor(Color.YELLOW)

        } else {
            holder.playerCard.setBackgroundColor(holder.originalBackgroundColor)
        }

        val current = players[position]
        holder.playerItemViewName.text = current.name
        var email = current.email?.trim()
        if (email.isNullOrBlank()) email = "-"
        holder.playerItemViewEmail.text = res.getString(R.string.email_descriptor, email)
    }

    internal fun getSelectedPlayer(): Player? {
        if (selectedPosition == defaultSelectedPosition) return null
        else return players[selectedPosition]
    }

    internal fun setPlayers(players: List<Player>) {
        this.players = players
        notifyDataSetChanged()
    }

    internal fun resetSelectedPosition() {
        var previousSelectedPosition = selectedPosition
        selectedPosition = defaultSelectedPosition
        notifyItemChanged(previousSelectedPosition)
        notifyItemChanged(selectedPosition)
    }

    /*
    private fun fetchColorOnBackground(): Int {
        val typedValue = TypedValue()
        val a: TypedArray =
            context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimary))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }
    */

    override fun getItemCount() = players.size
}