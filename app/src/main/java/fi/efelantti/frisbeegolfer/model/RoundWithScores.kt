package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.android.parcel.Parcelize

@Parcelize
class RoundWithScores(
    @Embedded val round: Round,
    @Relation(
        parentColumn = "roundId",
        entityColumn = "parentRoundId",
        entity = Score::class
    )
    var scores: List<ScoreWithPlayerAndHole>
) : Parcelable

