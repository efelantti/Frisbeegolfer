package fi.efelantti.frisbeegolfer.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.efelantti.frisbeegolfer.CourseListAdapter
import fi.efelantti.frisbeegolfer.EmptyRecyclerView
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModel
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModelFactory

class FragmentChooseCourse : Fragment(), CourseListAdapter.ListItemClickListener {

    private val courseViewModel: CourseViewModel by viewModels {
        CourseViewModelFactory((requireContext().applicationContext as FrisbeegolferApplication).repository)
    }

    interface FragmentChooseCourseListener {

        fun onCourseSelected(
            chosenCourseId: Long
        )
    }

    private lateinit var adapter: CourseListAdapter
    private var actionMode: ActionMode? = null
    private lateinit var recyclerView: EmptyRecyclerView
    private lateinit var emptyView: TextView
    private lateinit var fab: FloatingActionButton

    private val actionModeCallback = object : ActionMode.Callback {
        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.appbar_choose_course_or_players, menu)
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
            return false
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            fab.isEnabled = false
            adapter.resetSelectedPosition()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_choose_a_course, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CourseListAdapter(activity as Context, this)
        recyclerView = view.findViewById(
            R.id.recyclerview_choose_a_course
        )
        emptyView = view.findViewById(R.id.empty_view_choose_a_course)
        recyclerView.setEmptyView(emptyView)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        courseViewModel.allCourses.observe(viewLifecycleOwner, Observer { courses ->
            courses?.let { adapter.setCourses(it) }
        })

        fab = view.findViewById(R.id.fab_choose_course)
        fab.setOnClickListener {
            chooseSelectedCourse()
        }
    }

    private fun chooseSelectedCourse() {
        val course = adapter.getSelectedCourse()
        actionMode?.finish()
        if (course == null) throw java.lang.IllegalArgumentException("No course was selected.")
        sendBackResult(course.course.courseId)
    }

    // Call this method to send the data back to the parent activity
    private fun sendBackResult(chosenCourseId: Long) {
        // Notice the use of `getTargetFragment` which will be set when the dialog is displayed
        val listener: FragmentChooseCourseListener = activity as FragmentChooseCourseListener
        listener.onCourseSelected(chosenCourseId)
    }

    override fun onListItemClick(position: Int, shouldStartActionMode: Boolean) {
        if (!shouldStartActionMode) {
            actionMode?.finish()
            fab.isEnabled = false
        } else {
            fab.isEnabled = true
            when (actionMode) {
                null -> {
                    // Start the CAB using the ActionMode.Callback defined above
                    actionMode = activity?.startActionMode(actionModeCallback)
                    true
                }
                else -> false
            }
        }
    }
}