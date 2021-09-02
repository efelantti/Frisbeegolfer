package fi.efelantti.frisbeegolfer.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.efelantti.frisbeegolfer.*
import fi.efelantti.frisbeegolfer.databinding.FragmentCoursesBinding
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModel
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModelFactory

class FragmentCourses : Fragment(), CourseListAdapter.ListItemClickListener,
    DialogConfirmDelete.OnConfirmationSelected {

    private var _binding: FragmentCoursesBinding? = null
    private val binding get() = _binding!!
    private val courseViewModel: CourseViewModel by activityViewModels {
        CourseViewModelFactory((requireContext().applicationContext as FrisbeegolferApplication).repository)
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
                R.id.action_delete -> {
                    val course = adapter.getSelectedCourse()
                        ?: throw java.lang.IllegalArgumentException("No player was selected.")
                    deleteCourse(course)
                    mode.finish()
                    true
                }
                else -> false
            }
        }

        private fun editSelectedCourse() {
            val course = adapter.getSelectedCourse()
                ?: throw java.lang.IllegalArgumentException("No course was selected.")
            val action =
                FragmentCoursesDirections.actionFragmentCoursesToFragmentNewCourse(
                    NewCourseAction.EDIT.toString(),
                    course.course.courseId
                )
            findNavController().navigate(action)
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            fab.isEnabled = true
            adapter.resetSelectedPosition()
        }
    }

    private fun deleteCourse(course: CourseWithHoles) {
        DialogConfirmDelete(this, course, getString(R.string.course_type)).show(
            childFragmentManager, DialogConfirmDelete.TAG
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoursesBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CourseListAdapter(activity as Context, this)
        recyclerView = binding.recyclerviewCourses
        emptyView = binding.emptyViewCourses
        recyclerView.setEmptyView(emptyView)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        courseViewModel.allCourses.observe(viewLifecycleOwner, { list ->
            list?.let { courses ->
                val coursesSortedByCity = courses.sortedBy { it.course.city }
                adapter.setCourses(coursesSortedByCity)
            }
        })

        fab = binding.fabAddCourse
        fab.setOnClickListener {
            showNewCourseDialog()
        }
    }

    private fun showNewCourseDialog() {
        val fm: FragmentManager = parentFragmentManager
        val action =
            FragmentCoursesDirections.actionFragmentCoursesToFragmentNewCourse(
                NewCourseAction.ADD.toString(),
                -1L
            )
        findNavController().navigate(action)
    }

    override fun onListItemClick(position: Int, shouldStartActionMode: Boolean) {
        if (!shouldStartActionMode) {
            actionMode?.finish()
            fab.isEnabled = true
        } else {
            fab.isEnabled = false
            when (actionMode) {
                null -> {
                    // Start the CAB using the ActionMode.Callback defined above
                    actionMode = activity?.startActionMode(actionModeCallback)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
    }

    override fun returnUserConfirmation(objectToDelete: Any) {
        courseViewModel.delete(objectToDelete as CourseWithHoles)
    }
}