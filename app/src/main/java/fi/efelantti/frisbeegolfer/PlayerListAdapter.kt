package fi.efelantti.frisbeegolfer

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import fi.efelantti.frisbeegolfer.databinding.RecyclerviewPlayerWithEmailBinding
import fi.efelantti.frisbeegolfer.databinding.RecyclerviewPlayerWithoutEmailBinding
import fi.efelantti.frisbeegolfer.model.Player


class PlayerListAdapter internal constructor(
    val context: Context,
    onClickListener: ListItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface ListItemClickListener {
        fun onListItemClick(position: Int, shouldStartActionMode: Boolean)
    }

    private val res: Resources = context.resources
    private var players = emptyList<Player>()
    private var defaultSelectedPosition = -1
    var selectedPosition = defaultSelectedPosition
    private val mOnClickListener: ListItemClickListener = onClickListener
    private lateinit var builder: TextDrawable.IBuilder
    private val generator = ColorGenerator.MATERIAL

    override fun getItemViewType(position: Int): Int {
        return if (players[position].hasEmail()) Player.PlayerType.PlayerWithoutEmail.id
        else Player.PlayerType.PlayerWithEmail.id
    }

    inner class PlayerWithEmailViewHolder(binding: RecyclerviewPlayerWithEmailBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private val playerCard: ConstraintLayout = binding.playerItem
        private val playerIcon = binding.playerInitialImage
        private val playerItemViewName: TextView = binding.txtFullName
        private val playerItemViewEmail: TextView = binding.txtEmail

        init {
            itemView.setOnClickListener(this)
            builder = TextDrawable.builder()
                .beginConfig()
                .textColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.textDrawable_text_color,
                        null
                    )
                )
                .endConfig()
                .round()
        }

        override fun onClick(v: View?) {
            onClickHandler(bindingAdapterPosition)
        }

        fun bind(position: Int) {
            playerCard.isActivated = selectedPosition == position

            val current = players[position]
            val color = generator.getColor(current.name)
            val initial = current.name?.take(1)
            val icon = builder.build(initial, color)
            playerIcon.setImageDrawable(icon)
            playerItemViewName.text = current.name
            playerItemViewEmail.text = res.getString(R.string.email_descriptor, current.email)
        }
    }

    inner class PlayerWithoutEmailViewHolder(binding: RecyclerviewPlayerWithoutEmailBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private val playerCard: ConstraintLayout = binding.playerItem
        private val playerIcon = binding.playerInitialImage
        private val playerItemViewName: TextView = binding.txtFullName

        init {
            itemView.setOnClickListener(this)
            builder = TextDrawable.builder()
                .beginConfig()
                .textColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.textDrawable_text_color,
                        null
                    )
                )
                .endConfig()
                .round()
        }

        override fun onClick(v: View?) {
            onClickHandler(bindingAdapterPosition)
        }

        fun bind(position: Int) {
            playerCard.isActivated = selectedPosition == position

            val current = players[position]
            val color = generator.getColor(current.name)

            val initial = current.name?.take(1)
            val icon = builder.build(initial, color)
            playerIcon.setImageDrawable(icon)
            playerItemViewName.text = current.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Player.PlayerType.PlayerWithEmail.id -> {
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == Player.PlayerType.PlayerWithEmail.id) {
            (holder as PlayerWithEmailViewHolder).bind(position)
        } else {
            (holder as PlayerWithoutEmailViewHolder).bind(position)
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

    private fun onClickHandler(position: Int) {
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