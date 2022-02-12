package fi.efelantti.frisbeegolfer.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.LiveDataState
import fi.efelantti.frisbeegolfer.NewCourseAction
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.adapter.CourseListAdapter
import fi.efelantti.frisbeegolfer.databinding.FragmentCoursesBinding
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModel
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModelFactory

class FragmentCourses : SettingsMenuFragment(), CourseListAdapter.ListItemClickListener,
    DialogConfirmDelete.OnConfirmationSelected, SearchView.OnQueryTextListener {

    private var _binding: FragmentCoursesBinding? = null
    private val binding get() = _binding!!
    private val courseViewModel: CourseViewModel by activityViewModels {
        CourseViewModelFactory((requireContext().applicationContext as FrisbeegolferApplication).repository)
    }
    private lateinit var adapter: CourseListAdapter
    private var actionMode: ActionMode? = null
    private lateinit var recyclerView: RecyclerView
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val searchItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CourseListAdapter(activity as Context, this)
        recyclerView = binding.recyclerview

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        courseViewModel.state.observe(viewLifecycleOwner, { state ->
            when (state) {
                LiveDataState.LOADING -> binding.progressBar.visibility = View.VISIBLE
                LiveDataState.SUCCESS -> binding.progressBar.visibility = View.GONE
            }
        })

        courseViewModel.allCourses().observe(viewLifecycleOwner, { list ->
            list?.let { courses ->
                if (courses.count() == 0) {
                    binding.emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    binding.emptyView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    val sortedCourses = courses.sortedBy { it.course.city }
                    adapter.setCourses(sortedCourses)
                }
            }
        })

        fab = binding.fabAddCourse
        fab.setOnClickListener {
            showNewCourseDialog()
        }
    }

    private fun editSelectedCourse() {
        val course = adapter.getSelectedCourse()
        if (course != null) {
            val action =
                FragmentCoursesDirections.actionFragmentCoursesToFragmentNewCourse(
                    NewCourseAction.EDIT.toString(),
                    course.course.courseId
                )
            adapter.resetSelectedPosition()
            findNavController().navigate(action)
        }
    }

    private fun showNewCourseDialog() {
        val action =
            FragmentCoursesDirections.actionFragmentCoursesToFragmentNewCourse(
                NewCourseAction.ADD.toString(),
                -1L
            )
        findNavController().navigate(action)
    }

    override fun onListItemClick(position: Int, clickedOnSame: Boolean) {
        when (actionMode) {
            null -> {
                // Start the CAB using the ActionMode.Callback defined above
                editSelectedCourse()
            }
            else -> {
                onListItemLongClick(position, clickedOnSame)
            }
        }
    }

    override fun onListItemLongClick(position: Int, clickedOnSame: Boolean) {
        if (clickedOnSame) {
            actionMode?.finish()
            binding.fabAddCourse.isEnabled = true
        } else {
            binding.fabAddCourse.isEnabled = false
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

    override fun onQueryTextSubmit(query: String?): Boolean {
        val resultsCount = adapter.filter(query)
        actOnFilterResults(resultsCount)
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val resultsCount = adapter.filter(newText)
        actOnFilterResults(resultsCount)
        return true
    }

    private fun actOnFilterResults(resultsCount: Int) {
        if (resultsCount == 0 && adapter.getAllItemsCount() > 0 && courseViewModel.state.value == LiveDataState.SUCCESS) {
            binding.recyclerview.visibility = View.GONE
            binding.noMatches.visibility = View.VISIBLE
        } else {
            binding.recyclerview.visibility = View.VISIBLE
            binding.noMatches.visibility = View.GONE
        }
    }
}