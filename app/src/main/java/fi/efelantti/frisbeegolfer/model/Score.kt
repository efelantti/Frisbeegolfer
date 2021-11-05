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
    foreignKeys = [
        ForeignKey(
            entity = Round::class,
            parentColumns = arrayOf("dateStarted"),
            childColumns = arrayOf("parentRoundId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Player::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("playerId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Hole::class,
            parentColumns = arrayOf("holeId"),
            childColumns = arrayOf("holeId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
// TODO - Display DNF in the score card in some way.
@Parcelize
class Score(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(index = true)
    var parentRoundId: OffsetDateTime,
    @ColumnInfo(index = true)
    var playerId: Long = 0,
    @ColumnInfo(index = true)
    var holeId: Long = 0,
    var result: Int? = null,
    var isOutOfBounds: Boolean = false,
    var didNotFinish: Boolean = false
) : Parcelable {
    override fun equals(other: Any?): Boolean = (other is Score)
            && id == other.id
            && parentRoundId == other.parentRoundId
            && playerId == other.playerId
            && holeId == other.holeId
            && result == other.result

    override fun hashCode() = Objects.hash(id, parentRoundId, playerId, holeId, result)
}