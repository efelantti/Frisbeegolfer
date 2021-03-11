package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.time.OffsetDateTime

@Entity
@Parcelize
class Score(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var parentRoundId: OffsetDateTime,
    var playerId: Long = 0,
    var holeId: Long = 0,
    var result: Int = 0
): Parcelable