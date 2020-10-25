package fi.efelantti.frisbeegolfer

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
// TODO - Figure out how to parcelize
class Course(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String? = "",
    var city: String? = "",
    var holes: List<Hole>? = emptyList()
)