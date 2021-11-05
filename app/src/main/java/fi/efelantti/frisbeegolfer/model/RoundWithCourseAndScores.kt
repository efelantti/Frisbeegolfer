package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

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
{
    /**
     * Checks if round is not finished ie. any hole is still to be scored.
     */
    fun isFinished(): Boolean {
        // If a hole is not scored, its result is null. It might be null in the end as well,
        // but in that case, Did Not Finish flag should be set.
        return scores.any { it.score.result == null && !it.score.didNotFinish }
    }
}

