package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.time.OffsetDateTime
import java.util.*

@Entity
@Parcelize
class Score(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var parentRoundId: OffsetDateTime,
    var playerId: Long = 0,
    var holeId: Long = 0,
    var result: Int = 0
) : Parcelable {
    override fun equals(other: Any?): Boolean = (other is Score)
            && id == other.id
            && parentRoundId == other.parentRoundId
            && playerId == other.playerId
            && holeId == other.holeId
            && result == other.result

    override fun hashCode() = Objects.hash(id, parentRoundId, playerId, holeId, result)
}