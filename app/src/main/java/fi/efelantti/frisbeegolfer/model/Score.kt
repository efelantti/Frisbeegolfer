package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
class Score(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var parentRoundId: Long = 0,
    var playerId: Long = 0,
    var holeId: Long = 0,
    var result: Int = 0
): Parcelable