package fi.efelantti.frisbeegolfer

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import fi.efelantti.frisbeegolfer.databinding.RecyclerviewRoundBinding
import fi.efelantti.frisbeegolfer.model.RoundWithCourseAndScores
import java.time.format.DateTimeFormatter


class RoundListAdapter internal constructor(
    context: Context,
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

    inner class CourseViewHolder(binding: RecyclerviewRoundBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        val roundCard: CardView = binding.roundCard
        val roundItemViewStartedOn: TextView = binding.txtStartedOn
        val roundItemViewCourseName: TextView = binding.txtCourseNameRound
        val roundItemViewCity: TextView = binding.txtCityRound
        val roundItemViewNumberOfPlayers: TextView = binding.numberOfPlayers

        init {
            itemView.setOnClickListener(this)
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
        holder.roundItemViewStartedOn.text =
            current.round.dateStarted.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        holder.roundItemViewCourseName.text =
            res.getString(R.string.city, current.course.course.name)
        holder.roundItemViewCity.text = res.getString(R.string.city, current.course.course.city)
        holder.roundItemViewNumberOfPlayers.text =
            res.getString(
                R.string.players,
                current.scores.distinctBy { it.player.name }.map { it.player.name }.joinToString()
            )
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