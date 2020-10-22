package fi.efelantti.frisbeegolfer

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
class Player(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var firstName: String? = "",
    val nickName: String? = "",
    val lastName: String? = "",
    val email: String? = ""
) : Parcelable