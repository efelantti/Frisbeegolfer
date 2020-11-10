package fi.efelantti.frisbeegolfer.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
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

class ActivityCourses : AppCompatActivity(), FragmentNewCourse.FragmentNewCourseListener {

    private val TAG = "ActivityCourses"
    private val frisbeegolferViewModel: CourseViewModel by viewModels()

    private lateinit var recyclerView: EmptyRecyclerView
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courses)
        supportActionBar?.title = getString(R.string.courses_activity_title)

        var adapter = CourseListAdapter(this)
        recyclerView = findViewById<EmptyRecyclerView>(
            R.id.recyclerview_courses
        )
        emptyView = findViewById<TextView>(R.id.empty_view_courses)

        recyclerView.setEmptyView(emptyView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        frisbeegolferViewModel.allCourses.observe(this, Observer { courses ->
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

    private fun checkIfCourseAlreadyExists(course: CourseWithHoles, courses: List<CourseWithHoles>?): Boolean {
        if (courses == null) return false
        for(existingCourse: CourseWithHoles in courses) {
            if(CourseWithHoles.equals(
                    course,
                    existingCourse
                )
            ){
                Log.e(TAG, "Could not add course data to database - duplicate.")
                val toast = Toast.makeText(this, HtmlCompat.fromHtml("<font color='#FF0000' ><b>" + getString(
                    R.string.error_duplicate_course
                ) + "</b></font>", HtmlCompat.FROM_HTML_MODE_LEGACY), Toast.LENGTH_LONG)
                val indexOfCourse = courses.indexOf(existingCourse)
                recyclerView.scrollToPosition(indexOfCourse)
                toast.show()
                return true
            }
        }
        return false
    }

    override fun onCourseAdded(course: CourseWithHoles, result: Int) {
        if (result == Activity.RESULT_OK)
        {
            if(course == null) throw IllegalArgumentException("Course data was null.")
            else
            {
                val courses = frisbeegolferViewModel.allCourses.value
                var duplicateFound = checkIfCourseAlreadyExists(course, courses)
                if(!duplicateFound)
                {
                    frisbeegolferViewModel.insert(course)
                }
            }
        }
        else if(result == Activity.RESULT_CANCELED)
        {
            // Do nothing when canceled
        }
        else throw(IllegalArgumentException("Course data not returned from activity as expected."))
    }

    override fun onCourseEdited(course: CourseWithHoles, result: Int) {
        if (result == Activity.RESULT_OK)
        {
            if(course == null) throw IllegalArgumentException("Course data was null.")
            else
            {
                val courses = frisbeegolferViewModel.allCourses.value
                var duplicateFound = checkIfCourseAlreadyExists(course, courses)
                if(!duplicateFound)
                {
                    frisbeegolferViewModel.update(course)
                }
            }
        }
        else if(result == Activity.RESULT_CANCELED)
        {
            // Do nothing when canceled
        }
        else throw(IllegalArgumentException("Course data not returned from activity as expected."))
    }
}