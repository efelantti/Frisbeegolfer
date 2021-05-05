package fi.efelantti.frisbeegolfer.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import fi.efelantti.frisbeegolfer.*
import fi.efelantti.frisbeegolfer.databinding.FragmentNewCourseBinding
import fi.efelantti.frisbeegolfer.model.Course
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.Hole
import fi.efelantti.frisbeegolfer.model.clone
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModel
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModelFactory

class FragmentNewCourse : DialogFragment() {

    private val _tag = "FragmentNewCourse"
    private var _binding: FragmentNewCourseBinding? = null
    private val binding get() = _binding!!
    private val args: FragmentNewCourseArgs by navArgs()
    private var isFinalized = false
    private lateinit var scrollView: NestedScrollView
    private lateinit var courseNameLayout: TextInputLayout
    private lateinit var courseNameEditText: TextInputEditText
    private lateinit var cityLayout: TextInputLayout
    private lateinit var cityEditText: TextInputEditText
    private lateinit var numberOfHolesLayout: TextInputLayout
    private lateinit var numberOfHolesView: TextInputEditText
    private lateinit var recyclerView: EmptyRecyclerView
    private lateinit var applyHolesButton: Button
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
        saveMenuItem.isEnabled = isFinalized
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

        scrollView = binding.newCourseScrollview
        courseNameLayout = binding.courseNameLayout
        courseNameEditText = binding.editCourseName
        cityLayout = binding.editCityLayout
        cityEditText = binding.editCity
        numberOfHolesView = binding.editNumberOfHoles
        numberOfHolesLayout = binding.editNumberOfHolesLayout
        applyHolesButton = binding.applyHoles

        val adapter = HoleListAdapter(activity as Context)
        recyclerView = binding.recyclerviewHoles

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        lateinit var oldHolePars: List<Int>
        lateinit var oldHoleLengthMeter: List<Int?>

        val oldCourseId = args.courseId
        lateinit var saveAction: Toolbar.OnMenuItemClickListener

        if (actionCategory == NewCourseAction.ADD) {
            isFinalized = true
            requireActivity().invalidateOptionsMenu()
            oldHolePars = emptyList()
            oldHoleLengthMeter = emptyList()
            toolbar.title = getString(R.string.text_activity_new_course_title_add)
            newHoles = emptyList()
            adapter.setHoles(newHoles)
            saveAction = Toolbar.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_save -> {
                        createNewCourse()
                        return@OnMenuItemClickListener true
                    }
                }
                false
            }
            toolbar.setOnMenuItemClickListener(saveAction)
        } else if (actionCategory == NewCourseAction.EDIT) {
            courseViewModel.getCourseWithHolesById(oldCourseId)
                .observe(viewLifecycleOwner) { oldCourseData ->
                    if (oldCourseData != null) {
                        isFinalized = false
                        requireActivity().invalidateOptionsMenu()

                        applyHolesButton.isEnabled = false
                        numberOfHolesView.isEnabled = false

                        oldHolePars = oldCourseData.holes.map { it.par }
                        oldHoleLengthMeter = oldCourseData.holes.map { it.lengthMeters }
                        toolbar.title = getString(R.string.text_activity_new_course_title_edit)
                        courseNameEditText.setText(oldCourseData.course.name)
                        cityEditText.setText(oldCourseData.course.city)
                        oldCourseData.holes.let { adapter.setHoles(it) }
                        saveAction = Toolbar.OnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.action_save -> {
                                    editCourse(
                                        oldCourseData,
                                        oldHolePars,
                                        oldHoleLengthMeter
                                    )
                                    return@OnMenuItemClickListener true
                                }
                            }
                            false
                        }
                        toolbar.setOnMenuItemClickListener(saveAction)
                    }
                }
        }

        applyHolesButton.setOnClickListener {
            //applyHoles(numberOfHolesView, adapter)
            numberOfHolesView.onEditorAction(EditorInfo.IME_ACTION_DONE)
        }

        numberOfHolesView.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    applyHoles(numberOfHolesLayout, numberOfHolesView, adapter)
                    false
                }
                else -> false
            }
        }


        toolbar.setNavigationOnClickListener {
            showCancelDialog()
        }
    }

    /*
    Function that should be invoked when user clicks on the "Apply" (holes) button.
    Makes sure that the number of holes to set is valid. Also asks confirmation from the user before removing any holes.
    TODO - Scroll to first hole when button clicked?
     */
    private fun applyHoles(
        numberOfHolesLayout: TextInputLayout,
        numberOfHolesView: TextInputEditText,
        adapter: HoleListAdapter
    ) {
        val numberOfHolesToSet: Int? = numberOfHolesView.text.toString().toIntOrNull()
        val maximumNumberOfHoles: Int = resources.getInteger(R.integer.max_amount_of_holes)
        if (numberOfHolesToSet == null || numberOfHolesToSet <= 0 || numberOfHolesToSet > maximumNumberOfHoles) {
            numberOfHolesLayout.error =
                getString(R.string.invalid_number_of_holes, maximumNumberOfHoles)
        } else {
            numberOfHolesLayout.error = null
            val holesBefore = adapter.itemCount
            when {
                numberOfHolesToSet == holesBefore -> {
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
                    askConfirmationFromUserToRemoveHoles(adapter, holesToSet)
                }
            }
            //scrollView.requestChildFocus(recyclerView, recyclerView)
        }
    }

    /*
    If the new number of holes is less than before, some holes are removed. Before this is done, confirmation needs to be required from the user.
     */
    private fun askConfirmationFromUserToRemoveHoles(
        adapter: HoleListAdapter,
        holesToSet: List<Hole>
    ) {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.remove_holes))
            .setMessage(getString(R.string.dialog_message_confirm_remove_holes))
            .setPositiveButton(
                R.string.button_yes
            ) { _, _ ->
                adapter.setHoles(holesToSet)
            }
            .setNegativeButton(R.string.button_no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    /*
    Function for creating a new course and adding it to database.
    Parses the data from the UI, validates it and then adds the course to database, checking that it doesn't already exist.
     */
    private fun createNewCourse() {
        val courseName = courseNameEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val holes = getHoleInformation()
        val isValidCourseName = validateCourseName(courseName)
        val isValidCity = validateCity(city)
        val isValidNumberOfHoles = validateNumberOfHoles(holes)
        if (!isValidCourseName) {
            setFieldError(courseNameLayout, courseNameEditText)
        } else {
            courseNameLayout.error = null
        }
        if (!isValidCity) {
            setFieldError(cityLayout, cityEditText)
        } else {
            cityLayout.error = null
        }
        if (!isValidNumberOfHoles) {
            showInvalidNumberOfHolesMessage(numberOfHolesLayout)
        } else {
            numberOfHolesLayout.error = null
        }
        if (isValidCourseName && isValidCity && isValidNumberOfHoles) {
            courseViewModel.courseExists(courseName, city)
                .observe(viewLifecycleOwner) {
                    it?.let { duplicateFound ->
                        if (!duplicateFound) {
                            val courseData = CourseWithHoles(
                                course = Course
                                    (
                                    name = courseName,
                                    city = city
                                ),
                                holes = holes
                            )
                            courseViewModel.insert(courseData)
                            dismiss()
                        } else {
                            showCourseAlreadyExistsMessage()
                        }
                    }
                }
        }
    }

    /*
Function for editing an existing course and updating it in the database.
Parses the data from the UI, validates it and then udpates the course in the database, checking that it's new information doesn't already exist in the database.
 */
    private fun editCourse(
        oldCourseData: CourseWithHoles,
        oldHolePars: List<Int>,
        oldHoleLengthMeter: List<Int?>
    ) {
        val courseName = courseNameEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val holes = getHoleInformation()
        val isValidCourseName = validateCourseName(courseName)
        val isValidCity = validateCity(city)
        val isValidNumberOfHoles = validateNumberOfHoles(holes)
        if (!isValidCourseName) {
            setFieldError(courseNameLayout, courseNameEditText)
        } else {
            courseNameLayout.error = null
        }
        if (!isValidCity) {
            setFieldError(cityLayout, cityEditText)
        } else {
            cityLayout.error = null
        }
        if (!isValidNumberOfHoles) {
            showInvalidNumberOfHolesMessage(numberOfHolesLayout)
        } else {
            numberOfHolesLayout.error = null
        }
        if (isValidCourseName && isValidCity && isValidNumberOfHoles) {
            val courseData = CourseWithHoles(
                course = Course
                    (
                    courseId = oldCourseData.course.courseId,
                    name = courseName,
                    city = city
                ),
                holes = holes
            )
            if (CourseWithHoles.equals(
                    courseData,
                    oldCourseData
                ) && courseData.holes.count() == oldHolePars.count()
                && courseData.holes.withIndex()
                    .all { it.value.par == oldHolePars[it.index] }
                && courseData.holes.withIndex()
                    .all { it.value.lengthMeters == oldHoleLengthMeter[it.index] }
            ) {
                showDataNotEditedError()
            } else {
                showOverwriteDialog(oldCourseData, courseData, holes)
            }
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

    /*
    Validates that city name is OK.
     */
    private fun validateCity(city: String): Boolean {
        return !TextUtils.isEmpty(city)
    }

    /*
    Validates that course name is OK.
     */
    private fun validateCourseName(courseName: String): Boolean {
        return !TextUtils.isEmpty(courseName)
    }


    /*
    Validates that hole count is OK.
     */
    private fun validateNumberOfHoles(holes: List<Hole>): Boolean {
        return holes.count() > 0
    }

    /*
    Helper function for setting error text to a field. Assumes that field.hint contains name of the data.
     */
    private fun setFieldError(layout: TextInputLayout, field: TextInputEditText) {
        layout.error = getString(R.string.invalid_field, field.hint)
    }

    /*
    Displayed if the course already exists in the database.
     */
    private fun showCourseAlreadyExistsMessage() {
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
    }

    /*
    Displayed if the number of holes is not valid.
     */
    private fun showInvalidNumberOfHolesMessage(numberOfHolesLayout: TextInputLayout) {
        numberOfHolesLayout.error = getString(R.string.error_message_noHoles)
    }

    /*
    Asked when user clicks on Save, when editing an existing Course. Asks whether user wants to overwrite previous information.
    Also updates the course.
     */
    private fun showOverwriteDialog(
        oldCourseData: CourseWithHoles,
        newCourseData: CourseWithHoles,
        newHoles: List<Hole>
    ) {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.dialog_title_overwrite))
            .setMessage(getString(R.string.dialog_message_confirm_overwrite))
            .setPositiveButton(
                R.string.button_yes
            ) { _, _ ->
                // Update the course and the holes

                // No need to check for duplicates, if the course name or city name hasn't changed.
                if (oldCourseData.course.name == newCourseData.course.name && oldCourseData.course.city == newCourseData.course.city) {
                    oldCourseData.course.name = newCourseData.course.name
                    oldCourseData.course.city = newCourseData.course.city
                    oldCourseData.holes.forEachIndexed { index, hole ->
                        hole.par = newHoles[index].par
                        hole.lengthMeters = newHoles[index].lengthMeters
                    }
                    courseViewModel.update(oldCourseData)
                    dismiss()
                    // If the course name or city name has changed, then check for duplicates
                } else {
                    courseViewModel.courseExists(
                        newCourseData.course.name!!,
                        newCourseData.course.city!!
                    ).observe(viewLifecycleOwner) {
                        it?.let { duplicateFound ->
                            if (!duplicateFound) {
                                oldCourseData.course.name = newCourseData.course.name
                                oldCourseData.course.city = newCourseData.course.city
                                oldCourseData.holes.forEachIndexed { index, hole ->
                                    hole.par = newHoles[index].par
                                    hole.lengthMeters = newHoles[index].lengthMeters
                                    courseViewModel.update(oldCourseData)
                                    dismiss()
                                }
                            } else {
                                showCourseAlreadyExistsMessage()
                            }
                        }
                    }
                }
            }
            .setNegativeButton(R.string.button_no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    /*
    Shown when user clicks on the "Cancel" button.
     */
    private fun showCancelDialog() {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.dialog_title_cancel))
            .setMessage(getString(R.string.dialog_message_cancel))
            .setPositiveButton(
                R.string.button_yes
            ) { _, _ ->
                dismiss()
            }
            .setNegativeButton(R.string.button_no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    /*
    Displayed if user clicks on Save but nothing was changed.
     */
    private fun showDataNotEditedError() {
        Toast.makeText(
            context,
            getString(R.string.player_data_not_edited),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}