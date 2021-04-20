package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.android.parcel.Parcelize

@Parcelize
class RoundWithCourseAndScores(
    @Embedded val round: Round,
    @Relation(
        parentColumn = "dateStarted",
        entityColumn = "parentRoundId",
        entity = Score::class
    )
    var scores: List<ScoreWithPlayerAndHole>,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "courseId",
        entity = Course::class
    )
    var course: CourseWithHoles
) : Parcelable

