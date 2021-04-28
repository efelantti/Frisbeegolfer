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
import fi.efelantti.frisbeegolfer.databinding.RecyclerviewPlayerWithEmailBinding
import fi.efelantti.frisbeegolfer.databinding.RecyclerviewPlayerWithoutEmailBinding
import fi.efelantti.frisbeegolfer.model.Player


class PlayerListAdapter internal constructor(
    context: Context,
    onClickListener: ListItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface ListItemClickListener {
        fun onListItemClick(position: Int, shouldStartActionMode: Boolean)
    }

    enum class PlayerType(val id: Int) {
        PlayerWithEmail(id = 0),
        PlayerWithoutEmail(id = 1)
    }

    private val res: Resources = context.resources
    private var players = emptyList<Player>() // Cached copy of words
    private var defaultSelectedPosition = -1
    var selectedPosition = defaultSelectedPosition
    private val mOnClickListener: ListItemClickListener = onClickListener
    private lateinit var builder: TextDrawable.IBuilder
    private val generator = ColorGenerator.MATERIAL

    override fun getItemViewType(position: Int): Int {
        return if (players[position].email.isNullOrBlank()) PlayerType.PlayerWithoutEmail.id
        else PlayerType.PlayerWithEmail.id
    }

    inner class PlayerWithEmailViewHolder(binding: RecyclerviewPlayerWithEmailBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

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

    inner class PlayerWithoutEmailViewHolder(binding: RecyclerviewPlayerWithoutEmailBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        val playerCard: ConstraintLayout = binding.playerItem
        val originalBackgroundColor: Int = Color.WHITE
        val playerIcon = binding.playerInitialImage
        val playerItemViewName: TextView = binding.txtFullName

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PlayerType.PlayerWithEmail.id -> {
                val binding =
                    RecyclerviewPlayerWithEmailBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                PlayerWithEmailViewHolder(binding)
            }
            else -> {
                val binding =
                    RecyclerviewPlayerWithoutEmailBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                PlayerWithoutEmailViewHolder(binding)
            }
        }
    }

    // TODO - Change setBackgroundColor to Select?
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == PlayerType.PlayerWithEmail.id) {
            val playerWithEmailHolder = holder as PlayerWithEmailViewHolder
            if (selectedPosition == position) {
                playerWithEmailHolder.playerCard.setBackgroundColor(Color.YELLOW)

            } else {
                playerWithEmailHolder.playerCard.setBackgroundColor(playerWithEmailHolder.originalBackgroundColor)
            }

            val current = players[position]
            val color = generator.getColor(current.name)
            val initial = current.name?.take(1)
            val icon = builder.build(initial, color)
            playerWithEmailHolder.playerIcon.setImageDrawable(icon)
            playerWithEmailHolder.playerItemViewName.text = current.name
            var email = current.email?.trim()
            if (email.isNullOrBlank()) email = "-"
            playerWithEmailHolder.playerItemViewEmail.text =
                res.getString(R.string.email_descriptor, email)
        } else {
            val playerWithOutEmailHolder = holder as PlayerWithoutEmailViewHolder
            if (selectedPosition == position) {
                playerWithOutEmailHolder.playerCard.setBackgroundColor(Color.YELLOW)

            } else {
                playerWithOutEmailHolder.playerCard.setBackgroundColor(playerWithOutEmailHolder.originalBackgroundColor)
            }

            val current = players[position]
            val color = generator.getColor(current.name)
            val initial = current.name?.take(1)
            val icon = builder.build(initial, color)
            playerWithOutEmailHolder.playerIcon.setImageDrawable(icon)
            playerWithOutEmailHolder.playerItemViewName.text = current.name
        }

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