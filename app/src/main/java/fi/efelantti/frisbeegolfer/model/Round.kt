package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.OffsetDateTime
import java.util.*

@Entity(
    foreignKeys = [ForeignKey(
        entity = Course::class,
        parentColumns = arrayOf("courseId"),
        childColumns = arrayOf("courseId"),
        onDelete = ForeignKey.CASCADE
    )]
)
@Parcelize
class Round(
    @PrimaryKey
    var dateStarted: OffsetDateTime,
    @ColumnInfo(index = true)
    var courseId: Long
) : Parcelable {

    override fun equals(other: Any?): Boolean = (other is Round)
            && dateStarted == other.dateStarted
            && courseId == other.courseId

    override fun hashCode() = Objects.hash(dateStarted, courseId)
}