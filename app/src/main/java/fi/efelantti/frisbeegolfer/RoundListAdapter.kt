package fi.efelantti.frisbeegolfer

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import fi.efelantti.frisbeegolfer.model.RoundWithCourseAndScores
import java.time.format.DateTimeFormatter


class RoundListAdapter internal constructor(
    context: Context,
    onClickListener: ListItemClickListener
) : RecyclerView.Adapter<RoundListAdapter.CourseViewHolder>() {

    interface ListItemClickListener {
        fun onListItemClick(position: Int, shouldStartActionMode: Boolean)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val res: Resources = context.resources
    private var rounds = emptyList<RoundWithCourseAndScores>()
    private var defaultSelectedPosition = -1
    var selectedPosition = defaultSelectedPosition
    private val mOnClickListener: ListItemClickListener = onClickListener

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val roundCard: CardView = itemView.findViewById(R.id.roundCard)
        val originalBackgroundColor: Int = roundCard.cardBackgroundColor.defaultColor
        val roundItemViewStartedOn: TextView = itemView.findViewById(R.id.txtStartedOn)
        val roundItemViewCourseName: TextView = itemView.findViewById(R.id.txtCourseNameRound)
        val roundItemViewCity: TextView = itemView.findViewById(R.id.txtCityRound)
        val roundItemViewNumberOfPlayers: TextView = itemView.findViewById(R.id.numberOfPlayers)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            var shouldStartActionMode: Boolean
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
        val itemView = inflater.inflate(R.layout.recyclerview_round, parent, false)
        return CourseViewHolder(itemView)
    }

    // TODO - Change setBackgroundColor to Select?
    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        if (selectedPosition == position) {
            holder.roundCard.setBackgroundColor(Color.YELLOW)

        } else {
            holder.roundCard.setBackgroundColor(holder.originalBackgroundColor)
        }

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

    /*private fun fetchColorOnBackground(): Int {
        val typedValue = TypedValue()
        val a: TypedArray =
            context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimary))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }*/

    override fun getItemCount() = rounds.size
}