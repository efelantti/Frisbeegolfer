package fi.efelantti.frisbeegolfer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
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

    private val resources = context.resources
    private var rounds = emptyList<RoundWithCourseAndScores>()
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

        val current = rounds[position]
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

    internal fun setRounds(rounds: List<RoundWithCourseAndScores>) {
        this.rounds = rounds
        notifyDataSetChanged()
    }

    internal fun isRoundSelected(): Boolean {
        return selectedPosition == defaultSelectedPosition
    }

    internal fun getSelectedRound(): RoundWithCourseAndScores? {
        return if (selectedPosition == defaultSelectedPosition) null
        else rounds[selectedPosition]
    }

    internal fun resetSelectedPosition() {
        val previousSelectedPosition = selectedPosition
        selectedPosition = defaultSelectedPosition
        notifyItemChanged(previousSelectedPosition)
        notifyItemChanged(selectedPosition)
    }

    override fun getItemCount() = rounds.size
}