package fi.efelantti.frisbeegolfer.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fi.efelantti.frisbeegolfer.*
import fi.efelantti.frisbeegolfer.databinding.FragmentNewCourseBinding
import fi.efelantti.frisbeegolfer.model.Course
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.Hole
import fi.efelantti.frisbeegolfer.model.clone
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModel
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModelFactory

// TODO - Refactor into separate add and edit fragments.
class FragmentNewCourse : DialogFragment() {

    private val _tag = "FragmentNewCourse"
    private var _binding: FragmentNewCourseBinding? = null
    private val binding get() = _binding!!
    private val args: FragmentNewCourseArgs by navArgs()
    private var isFinalized = false
    private lateinit var courseNameView: EditText
    private lateinit var cityView: EditText
    private lateinit var recyclerView: EmptyRecyclerView
    private lateinit var newHoles: List<Hole>
    private val courseViewModel: CourseViewModel by activityViewModels {
        CourseViewModelFactory((requireContext().applicationContext as FrisbeegolferApplication).repository)
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_dialog, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val saveMenuItem = menu.findItem(R.id.action_save)
        if (isFinalized) {
            saveMenuItem.isEnabled = true
            saveMenuItem.icon.alpha = 255
        } else {
            saveMenuItem.isEnabled = false
            saveMenuItem.icon.alpha = 130
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewCourseBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = binding.dialogToolbarNewCourse
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.inflateMenu(R.menu.appbar_dialog)

        val actionCategory = NewCourseAction.valueOf(args.actionType)

        courseNameView = binding.editCourseName
        cityView = binding.editCity

        val adapter = HoleListAdapter(activity as Context)
        recyclerView = binding.recyclerviewHoles

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        lateinit var oldHolePars: List<Int>
        lateinit var oldHoleLengthMeter: List<Int?>

        val applyHolesButton: Button = binding.applyHoles
        val numberOfHolesView: EditText = binding.editNumberOfHoles

        val oldCourseId = args.courseId
        courseViewModel.getCourseWithHolesById(oldCourseId)
            .observe(viewLifecycleOwner) { oldCourseData ->
                if (actionCategory == NewCourseAction.ADD) {
                    isFinalized = true
                    requireActivity().invalidateOptionsMenu()
                    oldHolePars = emptyList()
                    oldHoleLengthMeter = emptyList()
                    toolbar.title = getString(R.string.text_activity_new_course_title_add)
                    newHoles = emptyList()
                    adapter.setHoles(newHoles)
                } else if (actionCategory == NewCourseAction.EDIT) {
                    if (oldCourseData != null) {
                        isFinalized = false
                        requireActivity().invalidateOptionsMenu()

                        applyHolesButton.isEnabled = false
                        numberOfHolesView.isEnabled = false

                        oldHolePars = oldCourseData.holes.map { it.par }
                        oldHoleLengthMeter = oldCourseData.holes.map { it.lengthMeters }
                        toolbar.title = getString(R.string.text_activity_new_course_title_edit)
                        courseNameView.setText(oldCourseData.course.name)
                        cityView.setText(oldCourseData.course.city)
                        oldCourseData.holes.let { adapter.setHoles(it) }
                    }
                }
                toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_save -> {
                            saveData(actionCategory, oldCourseData, oldHolePars, oldHoleLengthMeter)
                            return@OnMenuItemClickListener true
                        }
                    }
                    false
                })
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
                            ) { _, _ ->
                                adapter.setHoles(holesToSet)
                            } // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(R.string.button_no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show()
                    }
                }
            }
        }
        toolbar.setNavigationOnClickListener {
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.dialog_title_cancel))
                .setMessage(getString(R.string.dialog_message_cancel)) // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(
                    R.string.button_yes
                ) { _, _ ->
                    dismiss()
                } // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(R.string.button_no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
    }

    private fun saveData(
        actionCategory: NewCourseAction,
        oldCourseData: CourseWithHoles?,
        oldHolePars: List<Int>,
        oldHoleLengthMeter: List<Int?>
    ) {
        val requiredFields: List<EditText> = listOf(courseNameView, cityView)
        if (areValidFields(requiredFields)) {

            val courseName = courseNameView.text.toString().trim()
            val city = cityView.text.toString().trim()
            val holes = getHoleInformation()

            val courseData = CourseWithHoles(
                course = Course
                    (
                    name = courseName,
                    city = city
                ),
                holes = holes
            )

            if (actionCategory == NewCourseAction.EDIT && oldCourseData != null) {
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
                        ) { _, _ ->
                            // Update the course and the holes
                            oldCourseData.course.name = courseData.course.name
                            oldCourseData.course.city = courseData.course.city
                            oldCourseData.holes.forEachIndexed { index, hole ->
                                hole.par = holes[index].par
                                hole.lengthMeters = holes[index].lengthMeters
                            }
                            exitFragmentWithResult(actionCategory, oldCourseData)
                        } // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(R.string.button_no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                }
            } else {
                exitFragmentWithResult(actionCategory, courseData)
            }
        } else {
            Toast.makeText(
                context,
                getString(R.string.error_message_requiredFields),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Function to get hole attributes updated in the UI. Note that the ID should not be used (since
     * it is not taken from the UI).
     * TODO - View binding?
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
     * TODO - View binding?
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
    private fun exitFragmentWithResult(category: NewCourseAction?, courseData: CourseWithHoles?) {
        // Notice the use of `getTargetFragment` which will be set when the dialog is displayed
        when (category) {
            NewCourseAction.ADD -> {
                if (courseData != null) {
                    val courses = courseViewModel.allCourses.value
                    val duplicateFound = checkIfCourseAlreadyExists(courseData, courses)
                    if (!duplicateFound) {
                        courseViewModel.insert(courseData)
                        dismiss()
                    }
                }
            }
            NewCourseAction.EDIT -> {
                if (courseData != null) {
                    courseViewModel.update(courseData)
                    dismiss()
                }
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
            Log.e(_tag, "Could not add course data to database - duplicate.")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}