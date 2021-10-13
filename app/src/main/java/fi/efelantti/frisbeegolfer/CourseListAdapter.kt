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
import fi.efelantti.frisbeegolfer.databinding.RecyclerviewCourseBinding
import fi.efelantti.frisbeegolfer.model.CourseWithHoles


class CourseListAdapter internal constructor(
    val context: Context,
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
    private lateinit var builder: TextDrawable.IBuilder
    private val generator = ColorGenerator.MATERIAL

    inner class CourseViewHolder(val binding: RecyclerviewCourseBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val courseCard = binding.courseCard
        val icon = binding.courseAvatar
        val courseItemViewCourseName = binding.txtCourseName
        val courseItemViewCity = binding.txtCity

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
            RecyclerviewCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.courseCard.isActivated = selectedPosition == position

        val current = courses[position]
        holder.courseItemViewCourseName.text =
            res.getString(R.string.courseName, current.course.name)
        holder.courseItemViewCity.text = res.getString(R.string.city, current.course.city)
        val numberOfHoles = current.holes.count().toString()
        val color = generator.getColor(current.course.city)
        val icon = builder.build(numberOfHoles, color)
        holder.icon.setImageDrawable(icon)
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