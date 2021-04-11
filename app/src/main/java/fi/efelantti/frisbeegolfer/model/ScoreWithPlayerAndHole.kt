package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.android.parcel.Parcelize

@Parcelize
class ScoreWithPlayerAndHole(
    @Embedded val score: Score,
    @Relation(
        parentColumn = "playerId",
        entityColumn = "id"
    )
    var player: Player,
    @Relation(
        parentColumn = "holeId",
        entityColumn = "holeId"
    )
    var hole: Hole
) : Parcelable {
    override fun equals(other: Any?): Boolean = (other is ScoreWithPlayerAndHole)
            && score == other.score
            && player == other.player
            && hole == other.hole
}
