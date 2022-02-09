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


class PlayerListAdapterMultiSelect internal constructor(
    val context: Context,
    onClickListener: ListItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface ListItemClickListener {
        fun onListItemClick(position: Int, shouldStartActionMode: Boolean)
    }

    private val res: Resources = context.resources
    private var displayedPlayers = mutableListOf<Player>()
    private var allPlayers = mutableListOf<Player>()
    var selectedIndeces = mutableListOf<Int>()
    private val mOnClickListener: ListItemClickListener = onClickListener
    private lateinit var builder: TextDrawable.IBuilder
    private val generator = ColorGenerator.MATERIAL

    override fun getItemViewType(position: Int): Int {
        return if (displayedPlayers[position].hasEmail()) Player.PlayerType.PlayerWithoutEmail.id
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
            val selectedPlayer = displayedPlayers[position]
            playerCard.isActivated = selectedIndeces.contains(position)

            if (!playerCard.isActivated) {
                val color = generator.getColor(selectedPlayer.name)
                val initial = selectedPlayer.name?.take(1)
                val icon = builder.build(initial, color)
                playerIcon.setImageDrawable(icon)
            } else {
                playerIcon.setImageResource(R.drawable.recyclerview_selected_item_icon)
            }

            playerItemViewName.text = selectedPlayer.name
            playerItemViewEmail.text =
                res.getString(R.string.email_descriptor, selectedPlayer.email)
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
            val selectedPlayer = displayedPlayers[position]
            playerCard.isActivated = selectedIndeces.contains(position)

            if (!playerCard.isActivated) {
                val color = generator.getColor(selectedPlayer.name)
                val initial = selectedPlayer.name?.take(1)
                val icon = builder.build(initial, color)
                playerIcon.setImageDrawable(icon)
            } else {
                playerIcon.setImageResource(R.drawable.recyclerview_selected_item_icon)
            }
            playerItemViewName.text = selectedPlayer.name
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

    internal fun getSelectedPlayers(): List<Player> {
        return selectedIndeces.map { displayedPlayers[it] }
    }

    internal fun setPlayers(players: List<Player>) {
        this.displayedPlayers = players.toMutableList()
        this.allPlayers.addAll(displayedPlayers)
        notifyDataSetChanged()
    }

    fun filter(text: String?) {
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
    }

    internal fun resetSelectedPlayers() {
        val previousSelectedIndeces = selectedIndeces
        selectedIndeces = mutableListOf()
        for (index in previousSelectedIndeces) {
            notifyItemChanged(index)
        }
    }

    fun onClickHandler(position: Int) {
        displayedPlayers[position]
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

    override fun getItemCount() = displayedPlayers.size
}