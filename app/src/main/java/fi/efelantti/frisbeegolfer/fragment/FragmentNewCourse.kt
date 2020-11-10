package fi.efelantti.frisbeegolfer.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import fi.efelantti.frisbeegolfer.NewCourseAction
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.model.Course
import fi.efelantti.frisbeegolfer.model.CourseWithHoles

// TODO - Edit for course
// TODO - Replace hard coded strings with resource strings
class FragmentNewCourse : DialogFragment() {

    private lateinit var courseNameView: EditText
    private lateinit var cityView: EditText
    private lateinit var courseData: CourseWithHoles

    interface FragmentNewCourseListener {
        fun onCourseAdded(
            course: CourseWithHoles,
            result: Int
        )

        fun onCourseEdited(
            course: CourseWithHoles,
            result: Int
        )
    }
    companion object {
        fun newInstance(action: String, course: CourseWithHoles): FragmentNewCourse {
            val frag = FragmentNewCourse()
            val args = Bundle()
            args.putParcelable("courseData", course)
            args.putString("action", action)
            frag.setArguments(args)
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

        val actionCategory = arguments!!.getString("action")?.let { NewCourseAction.valueOf(it) }

        courseNameView = view.findViewById(R.id.edit_course_name)
        cityView = view.findViewById(R.id.edit_city)

        val oldCourseData = arguments!!.getParcelable<CourseWithHoles>("courseData")

        if (actionCategory == NewCourseAction.ADD)
        {
            toolbar.setTitle(getString(R.string.text_activity_new_course_title_add))
        }
        else if (actionCategory == NewCourseAction.EDIT)
        {
            toolbar.setTitle(getString(R.string.text_activity_new_course_title_edit))
            courseNameView.setText(oldCourseData?.course?.name)
            cityView.setText(oldCourseData?.course?.city)
        }

        toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_save -> {
                    val requiredFields: List<EditText> = listOf(courseNameView, cityView)
                    if (areValidFields(requiredFields)) {

                        val courseName = courseNameView.text.toString().trim()
                        val city = cityView.text.toString().trim()

                        courseData = CourseWithHoles(
                            course = Course
                            (
                                name = courseName,
                                city = city
                            ),
                            holes = emptyList()
                        )

                        if (actionCategory == NewCourseAction.EDIT) {
                            if(oldCourseData == null) throw IllegalArgumentException("Cannot edit player data - it was null.")
                            else courseData.course.courseId = oldCourseData.course.courseId // Take id from old course in order to update it to database.
                            if(CourseWithHoles.equals(
                                    courseData,
                                    oldCourseData
                                )
                            ){
                                Toast.makeText(context, getString(R.string.player_data_not_edited), Toast.LENGTH_LONG).show()
                            }
                            else{
                                AlertDialog.Builder(context)
                                    .setTitle(getString(R.string.dialog_title_overwrite))
                                    .setMessage(getString(R.string.dialog_message_confirm_overwrite)) // Specifying a listener allows you to take an action before dismissing the dialog.
                                    // The dialog is automatically dismissed when a dialog button is clicked.
                                    .setPositiveButton(
                                        R.string.button_yes,
                                        DialogInterface.OnClickListener { dialog, which ->
                                            sendBackResult(Activity.RESULT_OK, actionCategory)
                                        }) // A null listener allows the button to dismiss the dialog and take no further action.
                                    .setNegativeButton(R.string.button_no, null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show()
                            }
                        }
                        else {
                            sendBackResult(Activity.RESULT_OK, actionCategory)
                        }
                    }
                    else
                    {
                        Toast.makeText(context, getString(R.string.error_message_requiredFields), Toast.LENGTH_SHORT).show()
                    }
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
        toolbar.setNavigationOnClickListener{
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.dialog_title_cancel))
                .setMessage(getString(R.string.dialog_message_cancel)) // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(
                    R.string.button_yes,
                    DialogInterface.OnClickListener { dialog, which ->
                        courseData = CourseWithHoles(course = Course(), holes = emptyList())
                        sendBackResult(Activity.RESULT_CANCELED, actionCategory)
                    }) // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(R.string.button_no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
    }

    // Call this method to send the data back to the parent fragment
    fun sendBackResult(result: Int, category: NewCourseAction?) {
        // Notice the use of `getTargetFragment` which will be set when the dialog is displayed
        val listener: FragmentNewCourseListener = activity as FragmentNewCourseListener
        when(category)
        {
            NewCourseAction.ADD -> listener.onCourseAdded(courseData, result)
            NewCourseAction.EDIT -> listener.onCourseEdited(courseData, result)
        }
        dismiss()
    }

    private fun areValidFields(fields: List<EditText>): Boolean
    {
        var allFieldsValid: Boolean = true
        for (field: EditText in fields)
        {
            if(TextUtils.isEmpty(field.text.trim()))
            {
                field.setError(getString(R.string.invalid_field,field.hint))
                allFieldsValid = false
            }
        }
        return allFieldsValid
    }
}