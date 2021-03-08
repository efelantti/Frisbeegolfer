package fi.efelantti.frisbeegolfer

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import fi.efelantti.frisbeegolfer.fragment.FragmentNewPlayer
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.Player
import kotlinx.coroutines.selects.select


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

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val playerCard: CardView = itemView.findViewById(R.id.playerCard)
        val originalBackgroundColor: Int = playerCard.cardBackgroundColor.defaultColor
        val playerItemViewFullName: TextView = itemView.findViewById(R.id.txtFullName)
        val playerItemViewEmail: TextView = itemView.findViewById(R.id.txtEmail)

        init{
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
        if(current.nickName.isNullOrBlank()) {
            holder.playerItemViewFullName.text = res.getString(R.string.fullname_without_nickname, current.firstName, current.lastName)
        } else
            holder.playerItemViewFullName.text = res.getString(R.string.fullname_with_nickname, current.firstName, current.nickName, current.lastName)
        var email = current.email?.trim()
        if(email.isNullOrBlank()) email = "-"
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

    internal fun resetSelectedPosition()
    {
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