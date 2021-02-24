package fi.efelantti.frisbeegolfer.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fi.efelantti.frisbeegolfer.CourseListAdapter
import fi.efelantti.frisbeegolfer.EmptyRecyclerView
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.model.Course
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.Hole
import fi.efelantti.frisbeegolfer.model.clone
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModel

class FragmentChooseCourse : Fragment() {

    private val courseViewModel: CourseViewModel by viewModels()

    interface FragmentChooseCourseListener {

        fun onCourseSelected(
            chosenCourseId: Long,
            result: Int
        )
    }

    private lateinit var recyclerView: EmptyRecyclerView

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

        /* TODO - Toolbar
        val toolbar: Toolbar = view.findViewById(R.id.dialog_toolbar_new_course)
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.inflateMenu(R.menu.appbar_dialog)
         */

        var adapter = CourseListAdapter(activity as Context)
        recyclerView = view.findViewById<EmptyRecyclerView>(
            R.id.recyclerview_choose_a_course
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        courseViewModel.allCourses.observe(viewLifecycleOwner, Observer { courses ->
            courses?.let { adapter.setCourses(it) }
        })
   }

    // Call this method to send the data back to the parent activity
    fun sendBackResult(result: Int, chosenCourseId: Long) {
        // Notice the use of `getTargetFragment` which will be set when the dialog is displayed
        val listener: FragmentChooseCourseListener = activity as FragmentChooseCourseListener
        listener.onCourseSelected(chosenCourseId, result)
    }
}