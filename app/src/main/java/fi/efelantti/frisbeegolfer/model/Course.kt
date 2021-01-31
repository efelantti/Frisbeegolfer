package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
class Course(
    @PrimaryKey(autoGenerate = true)
    var courseId: Long = 0,
    var name: String? = "",
    var city: String? = ""
) : Parcelable {
    companion object {
        fun equals(course1: Course, course2: Course): Boolean {
            return course1.name == course2.name && course1.city == course2.city
        }
    }
}