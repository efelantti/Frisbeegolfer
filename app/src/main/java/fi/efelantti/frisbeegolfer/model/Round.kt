package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.OffsetDateTime
import java.util.*

@Entity
@Parcelize
class Round(
    @PrimaryKey
    var dateStarted: OffsetDateTime,
    var courseId: Long
) : Parcelable {

    override fun equals(other: Any?): Boolean = (other is Round)
            && dateStarted == other.dateStarted
            && courseId == other.courseId

    override fun hashCode() = Objects.hash(dateStarted, courseId)
}