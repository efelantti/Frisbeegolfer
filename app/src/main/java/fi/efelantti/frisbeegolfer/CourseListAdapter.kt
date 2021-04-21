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
import fi.efelantti.frisbeegolfer.databinding.RecyclerviewCourseBinding
import fi.efelantti.frisbeegolfer.model.CourseWithHoles


class CourseListAdapter internal constructor(
    context: Context,
    onClickListener: ListItemClickListener
) : RecyclerView.Adapter<CourseListAdapter.CourseViewHolder>() {

    interface ListItemClickListener {
        fun onListItemClick(position: Int, shouldStartActionMode: Boolean)
    }

    private val res: Resources = context.resources
    private var courses = emptyList<CourseWithHoles>()
    private var defaultSelectedPosition = -1
    var selectedPosition = defaultSelectedPosition
    private val mOnClickListener: ListItemClickListener = onClickListener

    inner class CourseViewHolder(val binding: RecyclerviewCourseBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val courseCard: CardView = binding.courseCard
        val originalBackgroundColor: Int = courseCard.cardBackgroundColor.defaultColor
        val courseItemViewCourseName: TextView = binding.txtCourseName
        val courseItemViewCity: TextView = binding.txtCity
        val courseItemViewNumberOfHoles: TextView = binding.txtNumberOfHoles

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
            RecyclerviewCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    // TODO - Change setBackgroundColor to Select?
    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        if (selectedPosition == position) {
            holder.courseCard.setBackgroundColor(Color.YELLOW)

        } else {
            holder.courseCard.setBackgroundColor(holder.originalBackgroundColor)
        }

        val current = courses[position]
        holder.courseItemViewCourseName.text = res.getString(R.string.courseName, current.course.name)
        holder.courseItemViewCity.text = res.getString(R.string.city, current.course.city)
        holder.courseItemViewNumberOfHoles.text = res.getString(R.string.numberOfHoles, current.holes.count())
    }

    internal fun setCourses(courses: List<CourseWithHoles>) {
        this.courses = courses
        notifyDataSetChanged()
    }

    internal fun getSelectedCourse(): CourseWithHoles? {
        return if (selectedPosition == defaultSelectedPosition) null
        else courses[selectedPosition]
    }

    internal fun resetSelectedPosition()
    {
        val previousSelectedPosition = selectedPosition
        selectedPosition = defaultSelectedPosition
        notifyItemChanged(previousSelectedPosition)
        notifyItemChanged(selectedPosition)
    }

    override fun getItemCount() = courses.size
}