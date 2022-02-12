package fi.efelantti.frisbeegolfer.adapter

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
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.databinding.RecyclerviewPlayerWithEmailBinding
import fi.efelantti.frisbeegolfer.databinding.RecyclerviewPlayerWithoutEmailBinding
import fi.efelantti.frisbeegolfer.model.Player


class PlayerListAdapter internal constructor(
    val context: Context,
    onClickListener: ListItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface ListItemClickListener {
        fun onListItemClick(position: Int, clickedOnSame: Boolean)
        fun onListItemLongClick(position: Int, clickedOnSame: Boolean)
    }

    private val res: Resources = context.resources
    private var displayedPlayers = mutableListOf<Player>()
    private var allPlayers = mutableListOf<Player>()
    private var defaultSelectedPosition = -1
    var selectedPosition = defaultSelectedPosition
    private val mOnClickListener: ListItemClickListener = onClickListener
    private lateinit var builder: TextDrawable.IBuilder
    private val generator = ColorGenerator.MATERIAL

    override fun getItemViewType(position: Int): Int {
        return if (displayedPlayers[position].hasEmail()) Player.PlayerType.PlayerWithoutEmail.id
        else Player.PlayerType.PlayerWithEmail.id
    }

    inner class PlayerWithEmailViewHolder(binding: RecyclerviewPlayerWithEmailBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        private val playerCard: ConstraintLayout = binding.playerItem
        private val playerIcon = binding.playerInitialImage
        private val playerItemViewName: TextView = binding.txtFullName
        private val playerItemViewEmail: TextView = binding.txtEmail

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
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
            val position: Int = bindingAdapterPosition
            val clickedOnSame: Boolean
            val previousSelectedPosition = selectedPosition
            if (selectedPosition == position) {
                resetSelectedPosition()
                clickedOnSame = true
            } else {
                selectedPosition = position
                notifyItemChanged(previousSelectedPosition)
                notifyItemChanged(selectedPosition)
                clickedOnSame = false
            }

            mOnClickListener.onListItemClick(position, clickedOnSame)
        }

        override fun onLongClick(v: View?): Boolean {
            val position: Int = bindingAdapterPosition
            val clickedOnSame: Boolean
            val previousSelectedPosition = selectedPosition
            if (selectedPosition == position) {
                resetSelectedPosition()
                clickedOnSame = true
            } else {
                selectedPosition = position
                notifyItemChanged(previousSelectedPosition)
                notifyItemChanged(selectedPosition)
                clickedOnSame = false
            }
            mOnClickListener.onListItemLongClick(position, clickedOnSame)
            return true
        }

        fun bind(position: Int) {
            playerCard.isActivated = selectedPosition == position

            val current = displayedPlayers[position]
            if (!playerCard.isActivated) {
                val color = generator.getColor(current.name)
                val initial = current.name?.take(1)
                val icon = builder.build(initial, color)
                playerIcon.setImageDrawable(icon)
            } else {
                playerIcon.setImageResource(R.drawable.recyclerview_selected_item_icon)
            }

            playerItemViewName.text = current.name
            playerItemViewEmail.text = res.getString(R.string.email_descriptor, current.email)
            playerIcon.setOnClickListener {
                onLongClick(it)
            }
        }
    }

    inner class PlayerWithoutEmailViewHolder(binding: RecyclerviewPlayerWithoutEmailBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        private val playerCard: ConstraintLayout = binding.playerItem
        private val playerIcon = binding.playerInitialImage
        private val playerItemViewName: TextView = binding.txtFullName

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
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
            val position: Int = bindingAdapterPosition
            val clickedOnSame: Boolean
            val previousSelectedPosition = selectedPosition
            if (selectedPosition == position) {
                resetSelectedPosition()
                clickedOnSame = true
            } else {
                selectedPosition = position
                notifyItemChanged(previousSelectedPosition)
                notifyItemChanged(selectedPosition)
                clickedOnSame = false
            }

            mOnClickListener.onListItemClick(position, clickedOnSame)
        }

        override fun onLongClick(v: View?): Boolean {
            val position: Int = bindingAdapterPosition
            val clickedOnSame: Boolean
            val previousSelectedPosition = selectedPosition
            if (selectedPosition == position) {
                resetSelectedPosition()
                clickedOnSame = true
            } else {
                selectedPosition = position
                notifyItemChanged(previousSelectedPosition)
                notifyItemChanged(selectedPosition)
                clickedOnSame = false
            }
            mOnClickListener.onListItemLongClick(position, clickedOnSame)
            return true
        }

        fun bind(position: Int) {
            playerCard.isActivated = selectedPosition == position

            val current = displayedPlayers[position]

            if (!playerCard.isActivated) {
                val color = generator.getColor(current.name)
                val initial = current.name?.take(1)
                val icon = builder.build(initial, color)
                playerIcon.setImageDrawable(icon)
            } else {
                playerIcon.setImageResource(R.drawable.recyclerview_selected_item_icon)
            }

            playerItemViewName.text = current.name
            playerIcon.setOnClickListener {
                onLongClick(it)
            }
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
        else displayedPlayers[selectedPosition]
    }

    internal fun setPlayers(players: List<Player>) {
        this.allPlayers = players.toMutableList()
        this.displayedPlayers = players.toMutableList()
        notifyDataSetChanged()
    }

    fun filter(text: String?): Int {
        if (text != null) {
            val filterText = text.toLowerCase()
            displayedPlayers.clear()
            if (text.isEmpty()) {
                displayedPlayers.addAll(allPlayers)
            } else {
                for (item in allPlayers) {
                    if (item.name?.toLowerCase()?.contains(filterText) == true ||
                        item.email?.toLowerCase()?.contains(filterText) == true
                    ) {
                        displayedPlayers.add(item)
                    }
                }
            }
            notifyDataSetChanged()
        }
        return displayedPlayers.count()
    }

    internal fun resetSelectedPosition() {
        val previousSelectedPosition = selectedPosition
        selectedPosition = defaultSelectedPosition
        notifyItemChanged(previousSelectedPosition)
        notifyItemChanged(selectedPosition)
    }

    internal fun getAllItemsCount() = allPlayers.size

    override fun getItemCount() = displayedPlayers.size
}