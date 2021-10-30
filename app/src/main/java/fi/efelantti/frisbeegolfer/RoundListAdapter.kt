package fi.efelantti.frisbeegolfer

import android.content.Context
import android.content.res.Resources
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
) : RecyclerView.Adapter<RoundListAdapter.CourseViewHolder>() {

    interface ListItemClickListener {
        fun onListItemClick(position: Int, shouldStartActionMode: Boolean)
    }

    private val res: Resources = context.resources
    private var rounds = emptyList<RoundWithCourseAndScores>()
    private var defaultSelectedPosition = -1
    var selectedPosition = defaultSelectedPosition
    private val mOnClickListener: ListItemClickListener = onClickListener
    private lateinit var builder: TextDrawable.IBuilder
    private val generator = ColorGenerator.MATERIAL

    inner class CourseViewHolder(binding: RecyclerviewRoundBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        val roundCard = binding.roundCard
        val roundIcon = binding.txtCourseNameAvatar
        val roundItemViewStartedOnDate = binding.txtStartedOnDate
        val roundItemViewStartedOnTime = binding.txtStartedOnTime
        val roundItemViewCourseName = binding.txtCourseNameRound
        val roundItemViewCity = binding.txtCityRound
        val roundItemViewPlayers = binding.nameOfPlayers

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
            val position: Int = bindingAdapterPosition
            val shouldStartActionMode: Boolean
            val previousSelectedPosition = selectedPosition
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding =
            RecyclerviewRoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.roundCard.isActivated = selectedPosition == position

        val current = rounds[position]
        val scores = current.scores
        val players = current.scores.distinctBy { it.player.name }.sortedBy { it.player.name }

        val color = generator.getColor(current.course.course.name)
        val initial = current.course.course.name?.take(1)
        val icon = builder.build(initial, color)
        holder.roundIcon.setImageDrawable(icon)
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
    }

    internal fun setRounds(rounds: List<RoundWithCourseAndScores>) {
        this.rounds = rounds
        notifyDataSetChanged()
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