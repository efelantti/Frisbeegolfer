package fi.efelantti.frisbeegolfer.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fi.efelantti.frisbeegolfer.*
import fi.efelantti.frisbeegolfer.model.Course
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.Hole
import fi.efelantti.frisbeegolfer.model.clone
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModel
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModelFactory

class FragmentNewCourse : DialogFragment() {

    private val TAG = "FragmentNewCourse"
    private lateinit var courseNameView: EditText
    private lateinit var cityView: EditText
    private lateinit var courseData: CourseWithHoles
    private lateinit var oldCourseData: CourseWithHoles
    private lateinit var recyclerView: EmptyRecyclerView
    private lateinit var newHoles: List<Hole>
    private val courseViewModel: CourseViewModel by viewModels {
        CourseViewModelFactory((requireContext().applicationContext as FrisbeegolferApplication).repository)
    }

    companion object {
        fun newInstance(action: String, course: CourseWithHoles): FragmentNewCourse {
            val frag = FragmentNewCourse()
            val args = Bundle()
            args.putParcelable("courseData", course)
            args.putString("action", action)
            frag.arguments = args
            return frag
        }
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_new_course, container)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = view.findViewById(R.id.dialog_toolbar_new_course)
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.inflateMenu(R.menu.appbar_dialog)

        val actionCategory =
            requireArguments().getString("action")?.let { NewCourseAction.valueOf(it) }

        courseNameView = view.findViewById(R.id.edit_course_name)
        cityView = view.findViewById(R.id.edit_city)

        oldCourseData = requireArguments().getParcelable("courseData")!!
        val oldHolePars = oldCourseData.holes.map { it.par }
        val oldHoleLengthMeter = oldCourseData.holes.map { it.lengthMeters }

        val adapter = HoleListAdapter(activity as Context)
        recyclerView = view.findViewById(
            R.id.recyclerview_holes
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        if (actionCategory == NewCourseAction.ADD) {
            toolbar.title = getString(R.string.text_activity_new_course_title_add)
            newHoles = emptyList()
            adapter.setHoles(newHoles)
        } else if (actionCategory == NewCourseAction.EDIT) {
            toolbar.title = getString(R.string.text_activity_new_course_title_edit)
            courseNameView.setText(oldCourseData.course.name)
            cityView.setText(oldCourseData.course.city)
            oldCourseData.holes.let { adapter.setHoles(it) }
        }

        val applyHolesButton: Button = view.findViewById(R.id.apply_holes)
        val numberOfHolesView: EditText = view.findViewById(R.id.edit_number_of_holes)
        // TODO - Should editing number of holes in an existing course be allowed?
        if (actionCategory == NewCourseAction.EDIT) {
            applyHolesButton.isEnabled = false
            numberOfHolesView.isEnabled = false
        }

        applyHolesButton.setOnClickListener {
            val numberOfHolesToSet: Int? = numberOfHolesView.text.toString().toIntOrNull()
            val maximumNumberOfHoles: Int = resources.getInteger(R.integer.max_amount_of_holes)
            if (numberOfHolesToSet == null || numberOfHolesToSet <= 0 || numberOfHolesToSet > maximumNumberOfHoles) {
                numberOfHolesView.error =
                    getString(R.string.invalid_number_of_holes, maximumNumberOfHoles)
            } else {
                val holesBefore = adapter.itemCount
                when {
                    numberOfHolesToSet == holesBefore -> {
                        // Nothing needs to be done
                    }
                    numberOfHolesToSet > holesBefore -> {
                        // Take existing holes and add new ones after them.
                        val holesToSet: List<Hole> =
                            getHoles(List(holesBefore) { Hole() }) + List(numberOfHolesToSet - holesBefore) { Hole() }
                        adapter.setHoles(holesToSet)
                    }
                    numberOfHolesToSet < holesBefore -> {
                        // Take existing holes and remove items from the end of the list.
                        val holesToSet: List<Hole> =
                            getHoles(List(holesBefore) { Hole() }).subList(0, numberOfHolesToSet)
                        // Require confirmation from user to remove holes
                        AlertDialog.Builder(context)
                            .setTitle(getString(R.string.remove_holes))
                            .setMessage(getString(R.string.dialog_message_confirm_remove_holes)) // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(
                                R.string.button_yes
                            ) { dialog, which ->
                                adapter.setHoles(holesToSet)
                            } // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(R.string.button_no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show()
                    }
                }
            }
        }

        toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_save -> {
                    val requiredFields: List<EditText> = listOf(courseNameView, cityView)
                    if (areValidFields(requiredFields)) {

                        val courseName = courseNameView.text.toString().trim()
                        val city = cityView.text.toString().trim()
                        val holes = getHoleInformation()

                        courseData = CourseWithHoles(
                            course = Course
                                (
                                name = courseName,
                                city = city
                            ),
                            holes = holes
                        )


                        if (actionCategory == NewCourseAction.EDIT) {
                            courseData.course.courseId =
                                oldCourseData.course.courseId // Take id from old course in order to update it to database.
                            if (CourseWithHoles.equals(
                                    courseData,
                                    oldCourseData
                                ) && courseData.holes.count() == oldHolePars.count()
                                && courseData.holes.withIndex()
                                    .all { it.value.par == oldHolePars[it.index] }
                                && courseData.holes.withIndex()
                                    .all { it.value.lengthMeters == oldHoleLengthMeter[it.index] }
                            ) {
                                Toast.makeText(
                                    context,
                                    getString(R.string.player_data_not_edited),
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                AlertDialog.Builder(context)
                                    .setTitle(getString(R.string.dialog_title_overwrite))
                                    .setMessage(getString(R.string.dialog_message_confirm_overwrite)) // Specifying a listener allows you to take an action before dismissing the dialog.
                                    // The dialog is automatically dismissed when a dialog button is clicked.
                                    .setPositiveButton(
                                        R.string.button_yes
                                    ) { dialog, which ->
                                        // Update the holes
                                        oldCourseData.holes.forEachIndexed { index, hole ->
                                            hole.par = holes[index].par
                                            hole.lengthMeters = holes[index].lengthMeters
                                        }
                                        exitFragmentWithResult(Activity.RESULT_OK, actionCategory)
                                    } // A null listener allows the button to dismiss the dialog and take no further action.
                                    .setNegativeButton(R.string.button_no, null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show()
                            }
                        } else {
                            exitFragmentWithResult(Activity.RESULT_OK, actionCategory)
                        }
                    } else {
                        Toast.makeText(
                            context,
                            getString(R.string.error_message_requiredFields),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
        toolbar.setNavigationOnClickListener {
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.dialog_title_cancel))
                .setMessage(getString(R.string.dialog_message_cancel)) // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(
                    R.string.button_yes
                ) { dialog, which ->
                    dismiss()
                } // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(R.string.button_no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
    }

    /**
     * Function to get hole attributes updated in the UI. Note that the ID should not be used (since
     * it is not taken from the UI).
     */
    private fun getHoleInformation(): List<Hole> {
        val listToReturn: MutableList<Hole> = mutableListOf()
        for (index: Int in 0 until recyclerView.childCount) {
            listToReturn.add(Hole())
            val viewHolder: RecyclerView.ViewHolder =
                recyclerView.findViewHolderForAdapterPosition(index)!!
            val view: View = viewHolder.itemView
            val textViewHoleNumber =
                view.findViewById<View>(R.id.recyclerView_hole_item_hole_index) as TextView
            val textViewPar = view.findViewById<View>(R.id.parCount) as TextView
            val textViewLength = view.findViewById<View>(R.id.edit_length) as TextView
            listToReturn[index].holeNumber = textViewHoleNumber.text.toString().toInt()
            listToReturn[index].par = textViewPar.text.toString().toInt()
            listToReturn[index].lengthMeters = textViewLength.text.toString().toIntOrNull()
        }
        return listToReturn
    }

    /**
     * Function to get the current values of the holes's attributes from the UI.
     */
    private fun getHoles(holes: List<Hole>): List<Hole> {
        val listToReturn: MutableList<Hole> = mutableListOf()
        //holes.forEach{listToReturn.add(it.clone())}
        for (index: Int in 0 until recyclerView.childCount) {
            listToReturn.add(holes[index].clone())
            val viewHolder: RecyclerView.ViewHolder =
                recyclerView.findViewHolderForAdapterPosition(index)!!
            val view: View = viewHolder.itemView
            val textViewHoleNumber =
                view.findViewById<View>(R.id.recyclerView_hole_item_hole_index) as TextView
            val textViewPar = view.findViewById<View>(R.id.parCount) as TextView
            val textViewLength = view.findViewById<View>(R.id.edit_length) as TextView
            listToReturn[index].holeNumber = textViewHoleNumber.text.toString().toInt()
            listToReturn[index].par = textViewPar.text.toString().toInt()
            listToReturn[index].lengthMeters = textViewLength.text.toString().toIntOrNull()
        }
        return listToReturn
    }

    // Call this method to send the data back to the parent fragment
    private fun exitFragmentWithResult(result: Int, category: NewCourseAction?) {
        // Notice the use of `getTargetFragment` which will be set when the dialog is displayed
        when (category) {
            NewCourseAction.ADD -> if (result == Activity.RESULT_OK) {
                val courses = courseViewModel.allCourses.value
                val duplicateFound = checkIfCourseAlreadyExists(courseData, courses)
                if (!duplicateFound) {
                    courseViewModel.insert(courseData)
                    dismiss()
                }
            } else throw(IllegalArgumentException("Course data not returned as expected."))
            NewCourseAction.EDIT -> when (result) {
                Activity.RESULT_OK -> {
                    courseViewModel.update(oldCourseData)
                    dismiss()
                }
                else -> throw(IllegalArgumentException("Course data not returned as expected."))
            }
        }
    }

    private fun areValidFields(fields: List<EditText>): Boolean {
        var allFieldsValid = true
        for (field: EditText in fields) {
            if (TextUtils.isEmpty(field.text.trim())) {
                field.error = getString(R.string.invalid_field, field.hint)
                allFieldsValid = false
            }
        }
        return allFieldsValid
    }

    fun addCourseToDatabase(course: CourseWithHoles, result: Int) {
        if (result == Activity.RESULT_OK) {
            val courses = courseViewModel.allCourses.value
            val duplicateFound = checkIfCourseAlreadyExists(course, courses)
            if (!duplicateFound) {
                courseViewModel.insert(course)
            }
        } else if (result == Activity.RESULT_CANCELED) {
        } else throw(IllegalArgumentException("Course data not returned from activity as expected."))
    }

    fun updateCourseInDatabase(course: CourseWithHoles, result: Int) {
        when (result) {
            Activity.RESULT_OK -> {
                courseViewModel.update(course)
            }
            Activity.RESULT_CANCELED -> {
            }
            else -> throw(IllegalArgumentException("Course data not returned from activity as expected."))
        }
    }

    private fun checkIfCourseAlreadyExists(
        course: CourseWithHoles,
        courses: List<CourseWithHoles>?
    ): Boolean {
        if (courses == null) return false
        var isDuplicate = false
        for (existingCourse: CourseWithHoles in courses) {
            if (Course.equals(course.course, existingCourse.course)) {
                isDuplicate = true
            }
        }
        if (isDuplicate) {
            Log.e(TAG, "Could not add course data to database - duplicate.")
            val toast = Toast.makeText(
                requireContext(), HtmlCompat.fromHtml(
                    "<font color='" + ContextCompat.getColor(
                        requireContext(),
                        R.color.colorErrorMessage
                    ) + "' ><b>" + getString(
                        R.string.error_duplicate_course
                    ) + "</b></font>", HtmlCompat.FROM_HTML_MODE_LEGACY
                ), Toast.LENGTH_LONG
            )
            toast.show()
            return true
        }
        return false
    }
}