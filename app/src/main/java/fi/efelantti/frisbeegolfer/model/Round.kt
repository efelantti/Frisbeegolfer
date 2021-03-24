package fi.efelantti.frisbeegolfer.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import fi.efelantti.frisbeegolfer.Converters
import kotlinx.android.parcel.Parcelize
import java.time.OffsetDateTime

@Entity
@Parcelize
class Round(
    @PrimaryKey
    var dateStarted: OffsetDateTime,
    var courseId: Long
) : Parcelable {
    companion object {
        fun equals(round1: Round, round2: Round): Boolean {
            //TODO - Implement if needed
            throw NotImplementedError()
        }
    }
}