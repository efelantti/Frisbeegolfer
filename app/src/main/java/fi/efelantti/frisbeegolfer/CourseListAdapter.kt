package fi.efelantti.frisbeegolfer

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import fi.efelantti.frisbeegolfer.fragment.FragmentNewCourse
import fi.efelantti.frisbeegolfer.model.CourseWithHoles


class CourseListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<CourseListAdapter.CourseViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val res: Resources = context.resources
    private var courses = emptyList<CourseWithHoles>() // Cached copy of words
    private val context = context
    private var selected_position = -1
    private var actionMode: ActionMode? = null

    private val actionModeCallback = object : ActionMode.Callback {
        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.appbar_actions, menu)
            mode.title = context.getString(R.string.course_selected)
            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_edit -> {
                    editSelectedCourse()
                    mode.finish() // Action picked, so close the CAB
                    true
                }
                else -> false
            }
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            notifyDataSetChanged()
            selected_position = -1
        }
    }

    private fun editSelectedCourse() {
        val course = courses[selected_position]
        val fm: FragmentManager = (context as FragmentActivity).supportFragmentManager
        val dialog: FragmentNewCourse = FragmentNewCourse.newInstance(NewCourseAction.EDIT.toString(), course)
        dialog.show(fm, "fragment_newPlayer")
    }


    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseCard: CardView = itemView.findViewById(R.id.courseCard)
        val originalBackgroundColor: Int = courseCard.cardBackgroundColor.defaultColor
        val courseItemViewCourseName: TextView = itemView.findViewById(R.id.txtCourseName)
        val courseItemViewCity: TextView = itemView.findViewById(R.id.txtCity)
        val courseItemViewNumberOfHoles: TextView = itemView.findViewById(R.id.txtNumberOfHoles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_course, parent, false)
        return CourseViewHolder(itemView)
    }

    // TODO - Change setBackgroundColor to Select?
    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        if (selected_position == position) {
            holder.courseCard.setBackgroundColor(Color.YELLOW)

        } else {
            holder.courseCard.setBackgroundColor(holder.originalBackgroundColor)
        }

         holder.courseCard.setOnClickListener(View.OnClickListener {
            if (selected_position === position) {
                selected_position = -1
                notifyDataSetChanged()
                actionMode?.finish()
                return@OnClickListener
            }
            selected_position = position
            notifyDataSetChanged()

            when (actionMode) {
                null -> {
                    // Start the CAB using the ActionMode.Callback defined above
                    actionMode = (context as Activity).startActionMode(actionModeCallback)
                    true
                }
                else -> false
            }
        })

        val current = courses[position]
        holder.courseItemViewCourseName.text = res.getString(R.string.courseName, current.course.name)
        holder.courseItemViewCity.text = res.getString(R.string.city, current.course.city)
        holder.courseItemViewNumberOfHoles.text = res.getString(R.string.numberOfHoles, current.holes.count())
    }

    internal fun setCourses(courses: List<CourseWithHoles>) {
        this.courses = courses
        notifyDataSetChanged()
    }

    private fun fetchColorOnBackground(): Int {
        val typedValue = TypedValue()
        val a: TypedArray =
            context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimary))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    override fun getItemCount() = courses.size
}