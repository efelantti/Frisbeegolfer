package fi.efelantti.frisbeegolfer

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import fi.efelantti.frisbeegolfer.databinding.RecyclerviewPlayerBinding
import fi.efelantti.frisbeegolfer.model.Player


class PlayerListAdapter internal constructor(
    context: Context,
    onClickListener: ListItemClickListener
) : RecyclerView.Adapter<PlayerListAdapter.PlayerViewHolder>() {

    interface ListItemClickListener {
        fun onListItemClick(position: Int, shouldStartActionMode: Boolean)
    }

    private val res: Resources = context.resources
    private var players = emptyList<Player>() // Cached copy of words
    private var defaultSelectedPosition = -1
    var selectedPosition = defaultSelectedPosition
    private val mOnClickListener: ListItemClickListener = onClickListener
    private lateinit var builder: TextDrawable.IBuilder
    private val generator = ColorGenerator.MATERIAL

    inner class PlayerViewHolder(binding: RecyclerviewPlayerBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        val playerCard: ConstraintLayout = binding.playerItem
        val originalBackgroundColor: Int = Color.WHITE
        val playerIcon = binding.playerInitialImage
        val playerItemViewName: TextView = binding.txtFullName
        val playerItemViewEmail: TextView = binding.txtEmail

        init {
            itemView.setOnClickListener(this)
            builder = TextDrawable.builder()
                .beginConfig()
                .endConfig()
                .round()
        }

        override fun onClick(v: View?) {
            val position: Int = bindingAdapterPosition
            val previousSelectedPosition = selectedPosition
            val shouldStartActionMode: Boolean
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
        val binding =
            RecyclerviewPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerViewHolder(binding)
    }

    // TODO - Change setBackgroundColor to Select?
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        if (selectedPosition == position) {
            holder.playerCard.setBackgroundColor(Color.YELLOW)

        } else {
            holder.playerCard.setBackgroundColor(holder.originalBackgroundColor)
        }

        val current = players[position]
        val color = generator.getColor(current.name)
        val initial = current.name?.take(1)
        val icon = builder.build(initial, color)
        holder.playerIcon.setImageDrawable(icon)
        holder.playerItemViewName.text = current.name
        var email = current.email?.trim()
        if (email.isNullOrBlank()) email = "-"
        holder.playerItemViewEmail.text = res.getString(R.string.email_descriptor, email)
    }

    internal fun getSelectedPlayer(): Player? {
        return if (selectedPosition == defaultSelectedPosition) null
        else players[selectedPosition]
    }

    internal fun setPlayers(players: List<Player>) {
        this.players = players
        notifyDataSetChanged()
    }

    internal fun resetSelectedPosition() {
        val previousSelectedPosition = selectedPosition
        selectedPosition = defaultSelectedPosition
        notifyItemChanged(previousSelectedPosition)
        notifyItemChanged(selectedPosition)
    }

    override fun getItemCount() = players.size
}