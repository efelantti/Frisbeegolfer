package fi.efelantti.frisbeegolfer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.databinding.RecyclerviewCourseBinding
import fi.efelantti.frisbeegolfer.model.CourseWithHoles


class CourseListAdapter internal constructor(
    val context: Context,
    onClickListener: ListItemClickListener
) : RecyclerView.Adapter<CourseListAdapter.CourseViewHolder>() {

    interface ListItemClickListener {
        fun onListItemClick(position: Int, clickedOnSame: Boolean)
        fun onListItemLongClick(position: Int, clickedOnSame: Boolean)
    }

    private val res: Resources = context.resources
    private var displayedCourses = mutableListOf<CourseWithHoles>()
    private var allCourses = mutableListOf<CourseWithHoles>()
    private var defaultSelectedPosition = -1
    var selectedPosition = defaultSelectedPosition
    private val mOnClickListener: ListItemClickListener = onClickListener
    private lateinit var builder: TextDrawable.IBuilder
    private val generator = ColorGenerator.MATERIAL

    inner class CourseViewHolder(val binding: RecyclerviewCourseBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {
        val courseCard = binding.courseCard
        val icon = binding.courseAvatar
        val courseItemViewCourseName = binding.txtCourseName
        val courseItemViewCity = binding.txtCity

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding =
            RecyclerviewCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.courseCard.isActivated = selectedPosition == position

        val current = displayedCourses[position]
        holder.courseItemViewCourseName.text =
            res.getString(R.string.courseName, current.course.name)
        holder.courseItemViewCity.text = res.getString(R.string.city, current.course.city)
        if (!holder.courseCard.isActivated) {
            val numberOfHoles = current.holes.count().toString()
            val color = generator.getColor(current.course.city)
            val icon = builder.build(numberOfHoles, color)
            holder.icon.setImageDrawable(icon)
        } else {
            holder.icon.setImageResource(R.drawable.recyclerview_selected_item_icon)
        }

        holder.icon.setOnClickListener {
            holder.onLongClick(it)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    internal fun setCourses(courses: List<CourseWithHoles>) {
        this.displayedCourses = courses.toMutableList()
        this.allCourses = courses.toMutableList()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(text: String?): Int {
        if (text != null) {
            val filterText = text.lowercase()
            displayedCourses.clear()
            if (text.isEmpty()) {
                displayedCourses.addAll(allCourses)
            } else {
                for (item in allCourses) {
                    if (item.course.name?.lowercase()
                            ?.contains(filterText) == true ||
                        item.course.city?.lowercase()
                            ?.contains(filterText) == true ||
                        filterText == item.holes.count().toString()
                    ) {
                        displayedCourses.add(item)
                    }
                }
            }
            notifyDataSetChanged()
        }
        return displayedCourses.count()
    }

    internal fun getSelectedCourse(): CourseWithHoles? {
        return if (selectedPosition == defaultSelectedPosition) null
        else allCourses[selectedPosition]
    }

    internal fun resetSelectedPosition() {
        val previousSelectedPosition = selectedPosition
        selectedPosition = defaultSelectedPosition
        notifyItemChanged(previousSelectedPosition)
        notifyItemChanged(selectedPosition)
    }

    internal fun getAllItemsCount() = allCourses.size

    override fun getItemCount() = displayedCourses.size
}