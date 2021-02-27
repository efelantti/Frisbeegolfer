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
import fi.efelantti.frisbeegolfer.model.CourseWithHoles


class CourseListAdapter internal constructor(
    context: Context,
    onClickListener: ListItemClickListener
) : RecyclerView.Adapter<CourseListAdapter.CourseViewHolder>() {

    interface ListItemClickListener {
        fun onListItemClick(position: Int, shouldStartActionMode: Boolean)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val res: Resources = context.resources
    private var courses = emptyList<CourseWithHoles>()
    private var defaultSelectedPosition = -1
    var selectedPosition = defaultSelectedPosition
    private val mOnClickListener: ListItemClickListener = onClickListener

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val courseCard: CardView = itemView.findViewById(R.id.courseCard)
        val originalBackgroundColor: Int = courseCard.cardBackgroundColor.defaultColor
        val courseItemViewCourseName: TextView = itemView.findViewById(R.id.txtCourseName)
        val courseItemViewCity: TextView = itemView.findViewById(R.id.txtCity)
        val courseItemViewNumberOfHoles: TextView = itemView.findViewById(R.id.txtNumberOfHoles)

        init{
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = getAdapterPosition()
            var shouldStartActionMode: Boolean
            if (selectedPosition == position) {
                resetSelectedPosition()
                notifyDataSetChanged()
                shouldStartActionMode = false
            } else {
                selectedPosition = position
                notifyDataSetChanged()
                shouldStartActionMode = true
            }
            mOnClickListener.onListItemClick(position, shouldStartActionMode)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_course, parent, false)
        return CourseViewHolder(itemView)
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
        if (selectedPosition == defaultSelectedPosition) return null
        else return courses[selectedPosition]

    }

    internal fun resetSelectedPosition()
    {
        selectedPosition = defaultSelectedPosition
    }

    /*private fun fetchColorOnBackground(): Int {
        val typedValue = TypedValue()
        val a: TypedArray =
            context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimary))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }*/

    override fun getItemCount() = courses.size
}