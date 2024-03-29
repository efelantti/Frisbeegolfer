package fi.efelantti.frisbeegolfer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.databinding.RecyclerviewRoundBinding
import fi.efelantti.frisbeegolfer.model.RoundWithCourseAndScores
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModel
import java.time.format.DateTimeFormatter


class RoundListAdapter internal constructor(
    val context: Context,
    onClickListener: ListItemClickListener
) : RecyclerView.Adapter<RoundListAdapter.RoundViewHolder>() {

    interface ListItemClickListener {
        fun onListItemClick(position: Int, clickedOnSame: Boolean)
        fun onListItemLongClick(position: Int, clickedOnSame: Boolean)
    }

    private var displayedRounds = mutableListOf<RoundWithCourseAndScores>()
    private var allRounds = mutableListOf<RoundWithCourseAndScores>()
    private var defaultSelectedPosition = -1
    var selectedPosition = defaultSelectedPosition
    private val mOnClickListener: ListItemClickListener = onClickListener
    private lateinit var builder: TextDrawable.IBuilder
    private val generator = ColorGenerator.MATERIAL

    inner class RoundViewHolder(binding: RecyclerviewRoundBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener,
        View.OnLongClickListener {
        val roundCard = binding.roundCard
        val roundIcon = binding.txtCourseNameAvatar
        val roundItemViewStartedOnDate = binding.txtStartedOnDate
        val roundItemViewStartedOnTime = binding.txtStartedOnTime
        val roundItemViewCourseName = binding.txtCourseNameRound
        val roundItemViewCity = binding.txtCityRound
        val roundItemViewPlayers = binding.nameOfPlayers

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoundViewHolder {
        val binding =
            RecyclerviewRoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoundViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoundViewHolder, position: Int) {
        holder.roundCard.isActivated = selectedPosition == position

        val current = displayedRounds[position]
        val scores = current.scores
        val players = current.scores.distinctBy { it.player.name }.sortedBy { it.player.name }

        if (!holder.roundCard.isActivated) {
            val color = generator.getColor(current.course.course.name)
            val initial = current.course.course.name?.take(1)
            val icon = builder.build(initial, color)
            holder.roundIcon.setImageDrawable(icon)
        } else {
            holder.roundIcon.setImageResource(R.drawable.recyclerview_selected_item_icon)
        }

        holder.roundItemViewCourseName.text = current.course.course.name
        holder.roundItemViewCity.text = current.course.course.city
        holder.roundItemViewStartedOnDate.text =
            current.round.dateStarted.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        holder.roundItemViewStartedOnTime.text =
            current.round.dateStarted.format(DateTimeFormatter.ofPattern("HH:mm"))
        holder.roundItemViewPlayers.text =
            players.joinToString {
                "${it.player.name} (${
                    ScoreViewModel.plusMinus(
                        it.player,
                        scores
                    )
                })"
            }

        holder.roundIcon.setOnClickListener {
            holder.onLongClick(it)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    internal fun setRounds(rounds: List<RoundWithCourseAndScores>) {
        this.displayedRounds = rounds.toMutableList()
        this.allRounds = rounds.toMutableList()
        notifyDataSetChanged()
    }

    // TODO - Add filter possibilities.
    // Date
    // more?
    @SuppressLint("NotifyDataSetChanged")
    fun filter(text: String?): Int {
        if (text != null) {
            val filterText = text.lowercase()
            displayedRounds.clear()
            if (text.isEmpty()) {
                displayedRounds.addAll(allRounds)
            } else {
                for (item in allRounds) {
                    if (item.course.course.name?.lowercase()
                            ?.contains(filterText) == true ||
                        item.course.course.city?.lowercase()
                            ?.contains(filterText) == true ||
                        item.scores.any {
                            it.player.name?.lowercase()
                                ?.contains(filterText) == true
                        } ||
                        item.scores.any {
                            it.player.email?.lowercase()
                                ?.contains(filterText) == true
                        }
                    ) {
                        displayedRounds.add(item)
                    }
                }
            }
            notifyDataSetChanged()
        }
        return displayedRounds.count()
    }

    internal fun getSelectedRound(): RoundWithCourseAndScores? {
        return if (selectedPosition == defaultSelectedPosition) null
        else displayedRounds[selectedPosition]
    }

    internal fun resetSelectedPosition() {
        val previousSelectedPosition = selectedPosition
        selectedPosition = defaultSelectedPosition
        notifyItemChanged(previousSelectedPosition)
        notifyItemChanged(selectedPosition)
    }

    fun getAllItemsCount() = allRounds.size

    override fun getItemCount() = displayedRounds.size
}