package fi.efelantti.frisbeegolfer.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.efelantti.frisbeegolfer.*
import fi.efelantti.frisbeegolfer.fragment.FragmentNewCourse
import fi.efelantti.frisbeegolfer.model.Course
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModel
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModelFactory


class ActivityCourses : AppCompatActivity(), FragmentNewCourse.FragmentNewCourseListener,
    CourseListAdapter.ListItemClickListener {

    private val TAG = "ActivityCourses"
    private val courseViewModel: CourseViewModel by viewModels {
        CourseViewModelFactory((applicationContext as FrisbeegolferApplication).repository)
    }

    private lateinit var recyclerView: EmptyRecyclerView
    private lateinit var adapter: CourseListAdapter
    private lateinit var emptyView: TextView
    private var actionMode: ActionMode? = null

    private val actionModeCallback = object : ActionMode.Callback {
        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.appbar_actions, menu)
            mode.title = getString(R.string.course_selected)
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

        private fun editSelectedCourse() {
            val course = adapter.getSelectedCourse()
            if (course == null) throw java.lang.IllegalArgumentException("No course was selected.")
            val fm: FragmentManager = supportFragmentManager
            val dialog: FragmentNewCourse =
                FragmentNewCourse.newInstance(NewCourseAction.EDIT.toString(), course)
            dialog.show(fm, "fragment_newPlayer")
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            adapter.resetSelectedPosition()
        }
    }

    override fun onListItemClick(position: Int, shouldStartActionMode: Boolean) {
        if (!shouldStartActionMode) {
            actionMode?.finish()
        } else {
            when (actionMode) {
                null -> {
                    // Start the CAB using the ActionMode.Callback defined above
                    actionMode = startActionMode(actionModeCallback)
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courses)
        supportActionBar?.title = getString(R.string.courses_activity_title)

        adapter = CourseListAdapter(this, this)
        recyclerView = findViewById(
            R.id.recyclerview_courses
        )
        emptyView = findViewById(R.id.empty_view_courses)

        recyclerView.setEmptyView(emptyView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        courseViewModel.allCourses.observe(this, Observer { courses ->
            // Update the cached copy of the words in the adapter.
            courses?.let { adapter.setCourses(it) }
        })

        val fab = findViewById<FloatingActionButton>(R.id.fab_add_course)
        fab.setOnClickListener {
            showNewCourseDialog()
        }
    }

    private fun showNewCourseDialog() {
        val fm: FragmentManager = supportFragmentManager
        val dialog: FragmentNewCourse =
            FragmentNewCourse.newInstance(
                NewCourseAction.ADD.toString(),
                CourseWithHoles(Course(), emptyList())
            )
        dialog.show(fm, "fragment_newCourse")
    }

    private fun checkIfCourseAlreadyExists(
        course: CourseWithHoles,
        courses: List<CourseWithHoles>?
    ): Boolean {
        if (courses == null) return false
        var isDuplicate = false
        var indexOfDuplicate = 0
        for (existingCourse: CourseWithHoles in courses) {
            if (Course.equals(course.course, existingCourse.course)) {
                isDuplicate = true
                indexOfDuplicate = courses.indexOf(existingCourse)
            }
        }
        if (isDuplicate) {
            Log.e(TAG, "Could not add course data to database - duplicate.")
            val toast = Toast.makeText(
                this, HtmlCompat.fromHtml(
                    "<font color='" + getColor(R.color.colorErrorMessage) + "' ><b>" + getString(
                        R.string.error_duplicate_course
                    ) + "</b></font>", HtmlCompat.FROM_HTML_MODE_LEGACY
                ), Toast.LENGTH_LONG
            )
            recyclerView.scrollToPosition(indexOfDuplicate)
            toast.show()
            return true
        }
        return false
    }

    override fun onCourseAdded(course: CourseWithHoles, result: Int) {
        if (result == Activity.RESULT_OK) {
            val courses = courseViewModel.allCourses.value
            val duplicateFound = checkIfCourseAlreadyExists(course, courses)
            if (!duplicateFound) {
                courseViewModel.insert(course)
            }
        } else if (result == Activity.RESULT_CANCELED) {
            this.recreate() // Recreate makes sure that the dialogfragment is recreated as well.
        } else throw(IllegalArgumentException("Course data not returned from activity as expected."))
    }

    override fun onCourseEdited(course: CourseWithHoles, result: Int) {
        when (result) {
            Activity.RESULT_OK -> {
                courseViewModel.update(course)
            }
            Activity.RESULT_CANCELED -> {
                this.recreate()
            }
            else -> throw(IllegalArgumentException("Course data not returned from activity as expected."))
        }
    }
}