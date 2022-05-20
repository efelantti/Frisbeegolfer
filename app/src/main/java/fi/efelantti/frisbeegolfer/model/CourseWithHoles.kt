package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
class CourseWithHoles(
    @Embedded val course: Course,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "parentCourseId"
    )
    var holes: List<Hole>
) : Parcelable
{
    companion object {
        fun equals(courseWithHoles1: CourseWithHoles, courseWithHoles2: CourseWithHoles): Boolean
        {
            return Course.equals(courseWithHoles1.course, courseWithHoles2.course) && Hole.equals(courseWithHoles1.holes, courseWithHoles2.holes)
        }
    }
}
