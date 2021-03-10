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


class PlayerListAdapterMultiSelect internal constructor(
    context: Context,
    onClickListener: PlayerListAdapterMultiSelect.ListItemClickListener
) : RecyclerView.Adapter<PlayerListAdapterMultiSelect.PlayerViewHolder>() {

    interface ListItemClickListener {
        fun onListItemClick(position: Int, shouldStartActionMode: Boolean)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val res: Resources = context.resources
    private var players = emptyList<Player>() // Cached copy of words
    var selectedIndeces = mutableListOf<Int>()
    private val mOnClickListener: PlayerListAdapterMultiSelect.ListItemClickListener = onClickListener

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
            val playerAtSelectedPosition = players[position]
            var shouldStartActionMode: Boolean
            if (selectedIndeces.contains(position)) {
                selectedIndeces.remove(position)
                notifyItemChanged(position)
                if (selectedIndeces.count() == 0) shouldStartActionMode = false
                else shouldStartActionMode = true
            } else {
                selectedIndeces.add(position)
                notifyItemChanged(position)
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
        val selectedPlayer = players[position]
        if (selectedIndeces.contains(position)) {
            holder.playerCard.setBackgroundColor(Color.YELLOW)

        } else {
            holder.playerCard.setBackgroundColor(holder.originalBackgroundColor)
        }

        if(selectedPlayer.nickName.isNullOrBlank()) {
            holder.playerItemViewFullName.text = res.getString(R.string.fullname_without_nickname, selectedPlayer.firstName, selectedPlayer.lastName)
        } else
            holder.playerItemViewFullName.text = res.getString(R.string.fullname_with_nickname, selectedPlayer.firstName, selectedPlayer.nickName, selectedPlayer.lastName)
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

    internal fun resetSelectedPlayers()
    {
        var previousSelectedIndeces = selectedIndeces
        selectedIndeces = mutableListOf<Int>()
        for(index in previousSelectedIndeces)
        {
            notifyItemChanged(index)
        }
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