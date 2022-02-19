package fi.efelantti.frisbeegolfer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.text.format.DateFormat
import java.time.OffsetDateTime

class DateTimePicker(
    val context: Context,
    private var pickTime: Boolean = false,
    var callback: (it: DateTimePicker) -> Unit
) {

    lateinit var pickedDateTime: OffsetDateTime

    fun show() {
        val currentDateTime = OffsetDateTime.now()
        val startYear = currentDateTime.year
        val startMonth = currentDateTime.month.value - 1
        val startDay = currentDateTime.dayOfMonth
        val startHour = currentDateTime.hour
        val startMinute = currentDateTime.minute

        val datePickerDialog = DatePickerDialog(context, { _, year, month, day ->
            if (pickTime) {
                TimePickerDialog(context, { _, hour, minute ->
                    pickedDateTime = OffsetDateTime.of(
                        year,
                        month + 1,
                        day,
                        hour,
                        minute,
                        0,
                        0,
                        OffsetDateTime.now().offset
                    )
                    callback(this)
                }, startHour, startMinute, DateFormat.is24HourFormat(context)).show()
            } else {
                pickedDateTime = OffsetDateTime.of(
                    year,
                    month + 1,
                    day,
                    0,
                    0,
                    0,
                    0,
                    OffsetDateTime.now().offset
                )
                callback(this)
            }
        }, startYear, startMonth, startDay)
        datePickerDialog.datePicker.firstDayOfWeek = Calendar.MONDAY
        datePickerDialog.show()
    }

    fun showTime() {
        val currentDateTime = OffsetDateTime.now()
        val startYear = currentDateTime.year
        val startMonth = currentDateTime.month.value - 1
        val startDay = currentDateTime.dayOfMonth
        val startHour = currentDateTime.hour
        val startMinute = currentDateTime.minute

        TimePickerDialog(context, { _, hour, minute ->
            pickedDateTime = OffsetDateTime.of(
                startYear,
                startMonth + 1,
                startDay,
                hour,
                minute,
                0,
                0,
                OffsetDateTime.now().offset
            )
            callback(this)
        }, startHour, startMinute, DateFormat.is24HourFormat(context)).show()
    }
}